package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAsAuraEffect;
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
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceAsAuraEffectHandler implements NormalEffectHandlerBean {

    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAsAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnSourceAsAuraEffect returnEffect = (ReturnSourceAsAuraEffect) effect;
        UUID cardId = entry.getCard().getId();
        UUID controllerId = entry.getControllerId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (sourceCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability does nothing (card is no longer in a graveyard)."));
            return;
        }

        Card auraCard = auraForm(sourceCard, returnEffect.enchantFilter());
        List<UUID> validTargetIds = new ArrayList<>();
        List<Permanent> controlledPermanents = gameData.playerBattlefields.get(controllerId);
        if (controlledPermanents != null) {
            for (Permanent permanent : controlledPermanents) {
                if (auraAttachmentService.canEnchant(gameData, auraCard, controllerId, permanent)) {
                    validTargetIds.add(permanent.getId());
                }
            }
        }

        if (validTargetIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " remains in its owner's graveyard (there is nothing it can enchant)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        if (validTargetIds.size() == 1) {
            attachAura(gameData, auraCard, controllerId, validTargetIds.getFirst());
            return;
        }

        gameData.interaction.setPendingAuraCard(auraCard);
        gameData.interaction.setPendingAuraOwnerId(controllerId);
        playerInputService.beginPermanentChoice(gameData, controllerId, validTargetIds,
                "Choose a Forest to attach " + auraCard.getName() + " to.");
    }

    private Card auraForm(Card sourceCard, com.github.laxika.magicalvibes.model.filter.TargetFilter enchantFilter) {
        Card copy = sourceCard.createRuntimeCopy();
        copy.setType(CardType.ENCHANTMENT);
        copy.setAdditionalTypes(Set.of());
        copy.setSubtypes(List.of(CardSubtype.AURA));
        copy.setPower(null);
        copy.setToughness(null);
        copy.clearRuntimeSpellTargets();
        copy.target(enchantFilter);
        copy.freeze();
        return copy;
    }

    private void attachAura(GameData gameData, Card auraCard, UUID controllerId, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !auraAttachmentService.canEnchant(gameData, auraCard, controllerId, target)) {
            return;
        }

        Permanent auraPermanent = new Permanent(auraCard);
        auraPermanent.setAttachedTo(targetId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, auraPermanent);

        gameLogService.append(gameData, GameLog.builder()
                .card(auraCard)
                .text(" returns to the battlefield attached to ")
                .card(target.getCard())
                .text(" under " + gameData.playerIdToName.get(controllerId) + "'s control.")
                .build());
        log.info("Game {} - {} returns as an Aura attached to {}", gameData.id,
                auraCard.getName(), target.getCard().getName());
    }
}
