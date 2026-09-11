package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOrAsNonAuraEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnSourceAuraToCreatureOrAsNonAuraEffectHandler implements NormalEffectHandlerBean {

    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAuraToCreatureOrAsNonAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnSourceAuraToCreatureOrAsNonAuraEffect returnEffect =
                (ReturnSourceAuraToCreatureOrAsNonAuraEffect) effect;
        UUID auraCardId = entry.getCard().getId();
        UUID auraControllerId = entry.getControllerId();
        UUID dyingCreatureControllerId = returnEffect.enchantedCreatureControllerId();

        if (dyingCreatureControllerId == null) {
            return;
        }

        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, auraCardId);
        if (auraCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            return;
        }

        List<UUID> validCreatureIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && auraAttachmentService.canEnchant(gameData, auraCard, auraControllerId, permanent)
                    && !gameQueryService.hasProtectionFromSource(gameData, permanent, auraCard)) {
                validCreatureIds.add(permanent.getId());
            }
        });

        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCardId);

        if (validCreatureIds.isEmpty()) {
            Card nonAuraCard = auraCard.createRuntimeCopy();
            nonAuraCard.setSubtypes(nonAuraCard.getSubtypes().stream()
                    .filter(subtype -> subtype != CardSubtype.AURA)
                    .toList());
            nonAuraCard.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new DealDamageToPlayerEffect(1, dyingCreatureControllerId));

            Permanent nonAuraPermanent = new Permanent(nonAuraCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraControllerId, nonAuraPermanent);
            gameLogService.append(gameData, GameLog.cardThen(auraCard,
                    " returns to the battlefield as a non-Aura enchantment."));
            return;
        }

        if (validCreatureIds.size() == 1) {
            attachAura(gameData, auraCard, auraControllerId, validCreatureIds.getFirst());
            return;
        }

        gameData.interaction.setPendingAuraCard(auraCard);
        gameData.interaction.setPendingAuraOwnerId(auraControllerId);
        playerInputService.beginPermanentChoice(gameData, dyingCreatureControllerId, validCreatureIds,
                "Choose a creature to attach " + auraCard.getName() + " to.");
    }

    private void attachAura(GameData gameData, Card auraCard, UUID auraControllerId, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        Permanent auraPermanent = new Permanent(auraCard);
        auraPermanent.setAttachedTo(target.getId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraControllerId, auraPermanent);
        gameLogService.append(gameData, GameLog.builder()
                .card(auraCard)
                .text(" returns to the battlefield attached to ")
                .card(target.getCard())
                .text(" under its controller's control.")
                .build());
    }
}
