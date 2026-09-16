package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedHerringExchangeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Resolves Red Herring's resolution-time exchange without moving the exchanged permanent. */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedHerringExchangeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedHerringExchangeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getCard();
        UUID controllerId = entry.getControllerId();
        if (!isInHand(gameData, controllerId, sourceCard)) {
            log.info("Game {} - Red Herring is no longer in hand", gameData.id);
            return;
        }

        List<UUID> choices = collectChoices(gameData, controllerId);
        if (choices.isEmpty()) {
            return;
        }
        if (choices.size() == 1) {
            exchange(gameData, choices.getFirst(), sourceCard, controllerId);
            return;
        }

        playerInputService.beginPermanentChoice(
                gameData,
                controllerId,
                choices,
                new PermanentChoiceContext.RedHerringExchange(sourceCard, controllerId),
                "Choose a permanent you control or a spell you control on the stack to exchange with Red Herring.");
    }

    public void completeChoice(GameData gameData, UUID chosenId,
                               PermanentChoiceContext.RedHerringExchange context) {
        exchange(gameData, chosenId, context.sourceCard(), context.controllerId());
    }

    private List<UUID> collectChoices(GameData gameData, UUID controllerId) {
        List<UUID> choices = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            choices.add(permanent.getId());
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (isControlledSpell(stackEntry, controllerId) && stackEntry.getTargetableId() != null) {
                choices.add(stackEntry.getTargetableId());
            }
        }
        return choices;
    }

    private void exchange(GameData gameData, UUID chosenId, Card sourceCard, UUID controllerId) {
        Card handCard = removeFromHand(gameData, controllerId, sourceCard);
        if (handCard == null) {
            log.info("Game {} - Red Herring left hand before the exchange resolved", gameData.id);
            return;
        }

        Permanent permanent = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(candidate -> candidate.getId().equals(chosenId))
                .findFirst()
                .orElse(null);
        if (permanent != null) {
            Card exchangedCard = permanent.getOriginalCard();
            addCardToOwnerHand(gameData, exchangedCard, controllerId);
            permanent.exchangeCard(handCard);
            gameLogService.append(gameData, GameLog.cardTextCard(
                    handCard, " exchanges with ", exchangedCard, "."));
            return;
        }

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> isControlledSpell(candidate, controllerId))
                .filter(candidate -> Objects.equals(candidate.getTargetableId(), chosenId))
                .findFirst()
                .orElse(null);
        if (spell == null) {
            // The choice was legal when offered, but the object left or changed before the answer.
            gameData.playerHands.get(controllerId).add(handCard);
            return;
        }

        UUID oldTargetableId = spell.getTargetableId();
        Card exchangedCard = spell.getPhysicalCard();
        addCardToOwnerHand(gameData, exchangedCard, spell.getOwnerId());
        spell.exchangeWithCardAsCreatureSpell(handCard);

        List<StackEntry> entriesThatChangedTarget = new ArrayList<>();
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry != spell && stackEntry.replaceTargetReferences(oldTargetableId, handCard.getId())) {
                entriesThatChangedTarget.add(stackEntry);
            }
        }
        if (gameData.pendingEffectResolutionEntry != null
                && gameData.pendingEffectResolutionEntry != spell
                && gameData.pendingEffectResolutionEntry.replaceTargetReferences(oldTargetableId, handCard.getId())) {
            entriesThatChangedTarget.add(gameData.pendingEffectResolutionEntry);
        }
        for (StackEntry targetingEntry : entriesThatChangedTarget) {
            if (targetingEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                    || targetingEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
                triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData, targetingEntry);
            } else {
                triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData, targetingEntry);
            }
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        gameLogService.append(gameData, GameLog.cardTextCard(
                handCard, " exchanges with ", exchangedCard, "."));
    }

    private Card removeFromHand(GameData gameData, UUID controllerId, Card sourceCard) {
        if (sourceCard == null) {
            return null;
        }
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null) {
            return null;
        }
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (sourceCard.getId().equals(card.getId())) {
                hand.remove(i);
                return card;
            }
        }
        return null;
    }

    private void addCardToOwnerHand(GameData gameData, Card card, UUID fallbackOwnerId) {
        if (card == null || card.isToken()) {
            return;
        }
        UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : fallbackOwnerId;
        if (ownerId != null && gameData.playerHands.containsKey(ownerId)) {
            gameData.addCardToHand(ownerId, card);
        }
    }

    private boolean isInHand(GameData gameData, UUID controllerId, Card card) {
        return card != null && gameData.playerHands.getOrDefault(controllerId, List.of()).stream()
                .anyMatch(handCard -> card.getId().equals(handCard.getId()));
    }

    private boolean isControlledSpell(StackEntry entry, UUID controllerId) {
        return entry.getCard() != null
                && controllerId.equals(entry.getControllerId())
                && !entry.isCopy()
                && switch (entry.getEntryType()) {
                    case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL,
                            BATTLE_SPELL, SORCERY_SPELL, INSTANT_SPELL -> true;
                    case ACTIVATED_ABILITY, TRIGGERED_ABILITY -> false;
                };
    }
}
