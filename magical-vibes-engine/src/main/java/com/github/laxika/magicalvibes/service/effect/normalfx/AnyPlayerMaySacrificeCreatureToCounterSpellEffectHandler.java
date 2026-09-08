package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreatureToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Brain Gorgers' any-player creature-sacrifice cast trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyPlayerMaySacrificeCreatureToCounterSpellEffectHandler implements NormalEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMaySacrificeCreatureToCounterSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTriggeringCardId();
        if (targetCardId == null) {
            return;
        }

        List<UUID> players = apnapPlayers(gameData);
        players.removeIf(playerId -> creatureIds(gameData, playerId).isEmpty());
        if (players.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyPlayerMaySacrificeCreatureToCounterSpellEffect(
                List.copyOf(players), entry.getControllerId(), targetCardId, false));
    }

    public List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return maySacrificeForCounterSupport.matchingPermanentIds(
                gameData, playerId, new PermanentIsCreaturePredicate());
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMaySacrificeCreatureToCounterSpellEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Sacrifice a creature? If a player does, counter " + sourceCard.getName() + ".",
                effect.targetCardId()));
        log.info("Game {} - offering {} the {} creature sacrifice choice", gameData.id,
                gameData.playerIdToName.get(playerId), sourceCard.getName());
    }

    public void sacrificeCreature(GameData gameData, UUID sacrificingPlayerId, UUID permanentId) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        if (creature != null) {
            destructionSupport.sacrificeAndLog(gameData, creature, sacrificingPlayerId);
        }
    }

    public void counterSpell(GameData gameData, Card sourceCard,
                             AnyPlayerMaySacrificeCreatureToCounterSpellEffect effect) {
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
                         AnyPlayerMaySacrificeCreatureToCounterSpellEffect effect,
                         UUID playerId, boolean anyAccepted) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        remaining.removeIf(id -> creatureIds(gameData, id).isEmpty());

        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyPlayerMaySacrificeCreatureToCounterSpellEffect(
                    List.copyOf(remaining), effect.abilityControllerId(), effect.targetCardId(), anyAccepted));
        }
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
