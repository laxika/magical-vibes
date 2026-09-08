package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayerAtUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceAuraToCreatureOrNonAuraOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect returnEffect =
                (ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect) effect;
        UUID auraCardId = entry.getCard().getId();
        UUID auraControllerId = entry.getControllerId();
        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, auraCardId);
        if (auraCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            return;
        }

        List<UUID> validTargetIds = findLegalCreatureIds(gameData, auraCard, auraControllerId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCardId);

        if (validTargetIds.isEmpty()) {
            returnAsNonAura(gameData, auraCard, auraControllerId, returnEffect.enchantedCreatureControllerId());
        } else if (validTargetIds.size() == 1) {
            attachAura(gameData, auraCard, auraControllerId, validTargetIds.getFirst());
        } else {
            gameData.interaction.setPendingAuraCard(auraCard);
            gameData.interaction.setPendingAuraOwnerId(auraControllerId);
            playerInputService.beginPermanentChoice(gameData,
                    returnEffect.enchantedCreatureControllerId(), validTargetIds,
                    "Choose a creature to attach " + auraCard.getName() + " to.");
        }
    }

    private List<UUID> findLegalCreatureIds(GameData gameData, Card auraCard, UUID auraControllerId) {
        List<UUID> validTargetIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && auraAttachmentService.canEnchant(gameData, auraCard, auraControllerId, permanent)) {
                    validTargetIds.add(permanent.getId());
                }
            }
        }
        return validTargetIds;
    }

    private void attachAura(GameData gameData, Card auraCard, UUID auraControllerId, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        Permanent auraPermanent = new Permanent(auraCard);
        auraPermanent.setAttachedTo(targetId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraControllerId, auraPermanent);
        gameLogService.append(gameData, GameLog.builder().card(auraCard)
                .text(" returns to the battlefield attached to ").card(target.getCard())
                .text(" under " + gameData.playerIdToName.get(auraControllerId) + "'s control.").build());
    }

    private void returnAsNonAura(GameData gameData, Card auraCard, UUID auraControllerId, UUID damagedPlayerId) {
        Card copy = auraCard.createRuntimeCopy();
        List<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
        subtypes.remove(CardSubtype.AURA);
        copy.setSubtypes(subtypes);
        copy.clearRuntimeSpellTargets();
        copy.setCardText("At the beginning of that player's upkeep, this enchantment deals 1 damage to that player.");
        copy.freeze();

        Permanent enchantment = new Permanent(auraCard);
        enchantment.setCard(copy);
        enchantment.addPersistentTriggeredEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DealDamageToPlayerAtUpkeepEffect(damagedPlayerId));
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraControllerId, enchantment);

        gameLogService.append(gameData, GameLog.cardThen(copy,
                " returns to the battlefield as a non-Aura enchantment under "
                        + gameData.playerIdToName.get(auraControllerId) + "'s control."));
        log.info("Game {} - {} returns as a non-Aura enchantment", gameData.id, auraCard.getName());
    }
}
