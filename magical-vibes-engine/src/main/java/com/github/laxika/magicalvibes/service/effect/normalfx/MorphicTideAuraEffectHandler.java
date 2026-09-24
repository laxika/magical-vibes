package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MorphicTideAuraEffect;
import com.github.laxika.magicalvibes.model.effect.MorphicTideBottomCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
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
public class MorphicTideAuraEffectHandler implements NormalEffectHandlerBean {

    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MorphicTideAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MorphicTideAuraEffect auraEffect = (MorphicTideAuraEffect) effect;
        Card auraCard = auraEffect.auraCard();
        UUID controllerId = auraEffect.ownerId();

        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, auraCard, Zone.LIBRARY)) {
            returnToBottom(entry, auraEffect);
            return;
        }

        List<UUID> validPermanentIds = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (auraAttachmentService.canEnchant(gameData, auraCard, controllerId, permanent)) {
                validPermanentIds.add(permanent.getId());
            }
        });
        List<UUID> validPlayerIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> auraAttachmentService.canEnchantPlayer(gameData, auraCard, controllerId, playerId))
                .toList();

        int validTargetCount = validPermanentIds.size() + validPlayerIds.size();
        if (validTargetCount == 0) {
            returnToBottom(entry, auraEffect);
        } else if (validTargetCount == 1) {
            putAttached(gameData, auraCard, controllerId,
                    validPermanentIds.isEmpty() ? validPlayerIds.getFirst() : validPermanentIds.getFirst());
        } else {
            gameData.interaction.setPendingAuraCard(auraCard);
            gameData.interaction.setPendingAuraOwnerId(controllerId);
            playerInputService.beginAnyTargetChoice(gameData, controllerId, validPermanentIds, validPlayerIds,
                    "Choose a permanent or player for " + auraCard.getName() + " to enchant.");
        }
    }

    private void putAttached(GameData gameData, Card auraCard, UUID controllerId, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(targetId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, aura);

        if (target != null && auraCard.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(ControlEnchantedCreatureEffect.class::isInstance)) {
            creatureControlService.applyControlEffect(gameData, controllerId, target,
                    new ControlEnchantedCreatureEffect(), EffectDuration.WHILE_ATTACHED,
                    aura.getId(), auraCard.getName());
        }

        GameLog.Builder log = GameLog.builder().card(auraCard)
                .text(" enters the battlefield attached to ");
        if (target != null) {
            log.card(target.getCard());
        } else {
            log.text(gameData.playerIdToName.get(targetId));
        }
        gameLogService.append(gameData, log.text(" under ")
                .text(gameData.playerIdToName.get(controllerId)).text("'s control.").build());
    }

    private void returnToBottom(StackEntry entry, MorphicTideAuraEffect auraEffect) {
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect next = entry.getEffectsToResolve().get(i);
            if (next instanceof MorphicTideBottomCardsEffect bottom
                    && bottom.playerId().equals(auraEffect.ownerId())) {
                List<Card> cards = new ArrayList<>(bottom.cards());
                cards.add(auraEffect.auraCard());
                entry.replaceEffectToResolve(i,
                        new MorphicTideBottomCardsEffect(bottom.playerId(), cards, bottom.randomOrder()));
                return;
            }
        }
        entry.insertEffectsToResolve(entry.getEffectsToResolve().size(), List.of(
                new MorphicTideBottomCardsEffect(
                        auraEffect.ownerId(), List.of(auraEffect.auraCard()), auraEffect.randomBottomOrder())));
    }
}
