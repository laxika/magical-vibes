package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBottomThenDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealUntilNonlandBottomThenDealManaValueDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandBottomThenDealManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var revealEffect = (RevealUntilNonlandBottomThenDealManaValueDamageEffect) effect;
        List<UUID> groupTargets = entry.targetsForBoundEffectGroup(revealEffect);
        UUID targetId = revealEffect.fixedTargetId();

        if (targetId == null && groupTargets != null) {
            if (groupTargets.isEmpty()) {
                return;
            }
            targetId = groupTargets.getFirst();
            insertFollowUpEffects(entry, revealEffect, groupTargets);
        } else if (targetId == null) {
            targetId = entry.getTargetId();
        }

        resolveForTarget(gameData, entry, targetId);
    }

    private void insertFollowUpEffects(StackEntry entry,
                                       RevealUntilNonlandBottomThenDealManaValueDamageEffect effect,
                                       List<UUID> targets) {
        if (targets.size() <= 1) {
            return;
        }
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Reveal effect is not in its stack entry");
        }
        List<CardEffect> followUps = targets.subList(1, targets.size()).stream()
                .map(target -> new RevealUntilNonlandBottomThenDealManaValueDamageEffect(
                        effect.targetPredicate(), target))
                .map(CardEffect.class::cast)
                .toList();
        entry.insertEffectsToResolve(effectIndex + 1, followUps);
    }

    private void resolveForTarget(GameData gameData, StackEntry entry, UUID targetId) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card nonland = null;
        while (!deck.isEmpty() && nonland == null) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (!card.hasType(CardType.LAND)) {
                nonland = card;
            }
        }

        String revealedNames = revealed.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + revealedNames
                + " from the top of their library with " + sourceName + "."));

        if (nonland != null) {
            int manaValue = nonland.getManaValue();
            gameLogService.append(gameData, GameLog.builder()
                    .text(sourceName + " deals damage equal to ")
                    .card(nonland)
                    .text("'s mana value (" + manaValue + ").")
                    .build());
            if (manaValue > 0 && targetId != null) {
                int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
                damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);
                gameOutcomeService.checkWinCondition(gameData);
            }
        }

        libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, revealed);
    }
}
