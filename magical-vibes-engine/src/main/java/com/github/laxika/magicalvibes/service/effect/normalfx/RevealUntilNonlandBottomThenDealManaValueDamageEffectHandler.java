package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBottomThenDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealUntilNonlandBottomThenDealManaValueDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final GameQueryService gameQueryService;

    /** "When you reveal a nonland card this way, [this] deals damage equal to that card's mana value to any target." */
    private static final CardEffect REFLEXIVE_DAMAGE = new DealDamageToAnyTargetEffect(new EventValue(), false, false);

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandBottomThenDealManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var revealEffect = (RevealUntilNonlandBottomThenDealManaValueDamageEffect) effect;
        List<UUID> groupTargets = entry.targetsForBoundEffectGroup(revealEffect);

        if (revealEffect.reflexive()) {
            resolveForTarget(gameData, entry, null, revealEffect.randomizeBottom(), true);
            return;
        }
        UUID targetId;
        List<UUID> targetsLeft = entry.getResolutionResults().pickedIds();
        if (!targetsLeft.isEmpty() && groupTargets != null && groupTargets.containsAll(targetsLeft)) {
            // "For each of them": a later run of this step, for the next of the targets the first run ordered.
            targetId = targetsLeft.getFirst();
            entry.recordPicks(targetsLeft.subList(1, targetsLeft.size()));
        } else if (groupTargets != null) {
            if (groupTargets.isEmpty()) {
                return;
            }
            List<UUID> orderedTargets = inTurnOrder(gameData, groupTargets);
            targetId = orderedTargets.getFirst();
            repeatForTargetsLeft(entry, revealEffect, orderedTargets);
        } else {
            targetId = entry.getTargetId();
        }

        resolveForTarget(gameData, entry, targetId, revealEffect.randomizeBottom(), false);
    }

    /**
     * Orders targets starting with the active player's own target and the permanents that player
     * controls, then the next player in turn order and so on (the 2022-09-09 ruling for this card);
     * targets of the same player keep the order they were chosen in.
     */
    private List<UUID> inTurnOrder(GameData gameData, List<UUID> targets) {
        List<UUID> turnOrder = gameData.orderedPlayerIds;
        int start = Math.max(0, turnOrder.indexOf(gameData.activePlayerId));
        return targets.stream()
                .sorted(java.util.Comparator.comparingInt(target -> {
                    UUID playerId = gameData.playerIds.contains(target) ? target
                            : gameQueryService.findPermanentController(gameData, target);
                    int index = turnOrder.indexOf(playerId);
                    return index < 0 ? turnOrder.size()
                            : Math.floorMod(index - start, turnOrder.size());
                }))
                .toList();
    }

    /**
     * Each target's reveal can stop for a choice (the order of the cards put on the bottom), so the card's step is
     * run once more for each target after the first: the targets still to do are the entry's picks, which each run
     * takes the next of.
     */
    private void repeatForTargetsLeft(StackEntry entry,
                                      RevealUntilNonlandBottomThenDealManaValueDamageEffect effect,
                                      List<UUID> targets) {
        if (targets.size() <= 1) {
            return;
        }
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Reveal effect is not in its stack entry");
        }
        entry.recordPicks(targets.subList(1, targets.size()));
        entry.insertEffectsToResolve(effectIndex + 1,
                java.util.Collections.<CardEffect>nCopies(targets.size() - 1, effect));
    }

    private void resolveForTarget(GameData gameData, StackEntry entry, UUID targetId,
                                  boolean randomizeBottom, boolean reflexive) {
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

        if (nonland != null && reflexive) {
            entry.setEventValue(nonland.getManaValue());
        } else if (nonland != null) {
            int manaValue = nonland.getManaValue();
            gameLogService.append(gameData, GameLog.builder()
                    .text(sourceName + " deals damage equal to ")
                    .card(nonland)
                    .text("'s mana value (" + manaValue + ").")
                    .build());
            if (manaValue > 0 && targetId != null) {
                int damage = manaValue;
                damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);
                gameOutcomeService.checkWinCondition(gameData);
            }
        }

        if (randomizeBottom) {
            Collections.shuffle(revealed);
            deck.addAll(revealed);
        } else {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, revealed);
        }
        if (nonland != null && reflexive) {
            // "Damage equal to that card's mana value": the ability records what the resolving entry was about, the
            // revealed card's mana value as its event value, whichever way it reaches the stack.
            List<CardEffect> damageEffects = List.of(REFLEXIVE_DAMAGE);
            ReflexiveAbilitySupport.route(gameData, ReflexiveAbilitySupport.record(gameData, entry, damageEffects),
                    () -> gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            entry.getCard(), entry.getControllerId(), damageEffects, false, null, 0,
                            entry.getSourcePermanentId())));
        }
    }
}
