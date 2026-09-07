package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayDiscardCardsToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a cast trigger that lets each player discard cards to counter the triggering spell. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMayDiscardCardsToCounterSpellEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayDiscardCardsToCounterSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AnyPlayerMayDiscardCardsToCounterSpellEffect discardEffect =
                (AnyPlayerMayDiscardCardsToCounterSpellEffect) effect;
        if (discardEffect.afterDiscard()) {
            counterSpell(gameData, entry.getCard(), discardEffect);
            promptRemainingPlayers(gameData, entry.getCard(), discardEffect);
            return;
        }

        UUID targetCardId = entry.getTriggeringCardId();
        if (targetCardId == null) {
            return;
        }

        List<UUID> players = apnapPlayers(gameData);
        players.removeIf(playerId -> !canDiscard(gameData, playerId, discardEffect.cardsToDiscard()));
        if (!players.isEmpty()) {
            promptNext(gameData, entry.getCard(), new AnyPlayerMayDiscardCardsToCounterSpellEffect(
                    discardEffect.cardsToDiscard(), players, entry.getControllerId(), targetCardId, false));
        }
    }

    public boolean canDiscard(GameData gameData, UUID playerId, int count) {
        return gameData.playerIds.contains(playerId)
                && gameData.playerHands.getOrDefault(playerId, List.of()).size() >= count;
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMayDiscardCardsToCounterSpellEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Discard " + effect.cardsToDiscard() + " cards to counter " + sourceCard.getName() + "?",
                effect.targetCardId()));
    }

    public void beginDiscard(GameData gameData, UUID playerId, Card sourceCard,
                             AnyPlayerMayDiscardCardsToCounterSpellEffect effect) {
        gameData.discardCausedByOpponent = !playerId.equals(effect.abilityControllerId());
        AnyPlayerMayDiscardCardsToCounterSpellEffect afterDiscard =
                new AnyPlayerMayDiscardCardsToCounterSpellEffect(
                        effect.cardsToDiscard(),
                        remainingPlayersAfter(gameData, effect, playerId),
                        effect.abilityControllerId(),
                        effect.targetCardId(),
                        true);
        playerInputService.beginDiscardChoice(gameData, playerId, effect.cardsToDiscard(),
                DiscardFollowUp.thenEffect(sourceCard, afterDiscard));
    }

    public void counterSpell(GameData gameData, Card sourceCard,
                             AnyPlayerMayDiscardCardsToCounterSpellEffect effect) {
        StackEntry sourceEntry = gameData.pendingEffectResolutionEntry;
        if (sourceEntry == null) {
            sourceEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    effect.abilityControllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)));
        }
        StackEntry targetEntry = counterSupport.findCounterTargetExcludingSource(
                gameData, effect.targetCardId(), sourceEntry);
        if (targetEntry != null) {
            counterSupport.counterSpell(gameData, sourceEntry, targetEntry);
        }
    }

    public void advance(GameData gameData, Card sourceCard,
                         AnyPlayerMayDiscardCardsToCounterSpellEffect effect, UUID playerId) {
        List<UUID> remaining = remainingPlayersAfter(gameData, effect, playerId);
        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyPlayerMayDiscardCardsToCounterSpellEffect(
                    effect.cardsToDiscard(), remaining, effect.abilityControllerId(),
                    effect.targetCardId(), false));
        }
    }

    private void promptRemainingPlayers(GameData gameData, Card sourceCard,
                                        AnyPlayerMayDiscardCardsToCounterSpellEffect effect) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.removeIf(playerId -> !canDiscard(gameData, playerId, effect.cardsToDiscard()));
        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyPlayerMayDiscardCardsToCounterSpellEffect(
                    effect.cardsToDiscard(), remaining, effect.abilityControllerId(),
                    effect.targetCardId(), false));
        }
    }

    private List<UUID> remainingPlayersAfter(GameData gameData,
                                              AnyPlayerMayDiscardCardsToCounterSpellEffect effect,
                                              UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !canDiscard(gameData, id, effect.cardsToDiscard()));
        return remaining;
    }

    private static List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.size());
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            return rotated;
        }
        return ordered;
    }
}
