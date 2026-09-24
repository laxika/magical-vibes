package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Gilded Ambusher's attack-triggered exile and damage ability. */
@Component
@RequiredArgsConstructor
public class EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var ambusherEffect =
                (EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect) effect;
        beginNextOpponent(gameData, entry, apnapOpponents(gameData, entry.getControllerId()),
                ambusherEffect.permanentFilter());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.GildedAmbusherChoice context) {
        StackEntry entry = context.resolvingEntry();
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !context.opponentId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !matches(gameData, chosen, context.permanentFilter(), entry)) {
            throw new IllegalStateException(
                    "Chosen permanent is no longer a legal Gilded Ambusher exile target");
        }

        processOpponent(gameData, entry, context.opponentId(), context.remainingOpponentIds(),
                context.permanentFilter(), chosen);
    }

    private void beginNextOpponent(GameData gameData, StackEntry entry, List<UUID> remainingOpponentIds,
                                   PermanentPredicate permanentFilter) {
        if (remainingOpponentIds.isEmpty()) {
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        UUID opponentId = remainingOpponentIds.getFirst();
        List<UUID> rest = remainingOpponentIds.size() > 1
                ? List.copyOf(remainingOpponentIds.subList(1, remainingOpponentIds.size())) : List.of();
        List<UUID> candidates = matchingPermanentIds(gameData, opponentId, permanentFilter, entry);
        if (candidates.size() > 1) {
            PermanentChoiceContext.GildedAmbusherChoice context =
                    new PermanentChoiceContext.GildedAmbusherChoice(
                            entry, opponentId, rest, permanentFilter);
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, opponentId, candidates, context,
                    entry.getCard().getName()
                            + " — Choose a nonland, nontoken permanent you control to exile.");
            return;
        }

        Permanent chosen = candidates.isEmpty() ? null
                : gameQueryService.findPermanentById(gameData, candidates.getFirst());
        processOpponent(gameData, entry, opponentId, rest, permanentFilter, chosen);
    }

    private void processOpponent(GameData gameData, StackEntry entry, UUID opponentId,
                                 List<UUID> remainingOpponentIds, PermanentPredicate permanentFilter,
                                 Permanent chosenPermanent) {
        int totalManaValue = 0;
        if (chosenPermanent != null
                && opponentId.equals(gameQueryService.findPermanentController(gameData, chosenPermanent.getId()))
                && matches(gameData, chosenPermanent, permanentFilter, entry)) {
            totalManaValue += chosenPermanent.getCard().getManaValue();
            exileSupport.exilePermanentAndLog(gameData, chosenPermanent, entry.getCard().getName());
        }

        Card randomNonland = exileRandomNonlandCard(gameData, opponentId);
        if (randomNonland != null) {
            totalManaValue += randomNonland.getManaValue();
            gameLogService.append(gameData, GameLog.cardThen(randomNonland,
                    " is exiled at random from " + gameData.playerIdToName.get(opponentId)
                            + "'s library by " + entry.getCard().getName() + "."));
        }

        if (totalManaValue > 0) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, totalManaValue, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, opponentId, damage);
        }
        beginNextOpponent(gameData, entry, remainingOpponentIds, permanentFilter);
    }

    private Card exileRandomNonlandCard(GameData gameData, UUID playerId) {
        List<Card> library = gameData.playerDecks.getOrDefault(playerId, List.of());
        List<Card> nonlands = library.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (nonlands.isEmpty()) {
            return null;
        }

        Card chosen = nonlands.get(ThreadLocalRandom.current().nextInt(nonlands.size()));
        if (!library.removeIf(card -> card.getId().equals(chosen.getId()))) {
            return null;
        }
        exileService.exileCard(gameData, playerId, chosen);
        return chosen;
    }

    private List<UUID> matchingPermanentIds(GameData gameData, UUID playerId,
                                            PermanentPredicate permanentFilter, StackEntry entry) {
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> ids = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (matches(permanent, permanentFilter, context)) {
                ids.add(permanent.getId());
            }
        }
        return ids;
    }

    private boolean matches(GameData gameData, Permanent permanent, PermanentPredicate filter, StackEntry entry) {
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());
        return matches(permanent, filter, context);
    }

    private boolean matches(Permanent permanent, PermanentPredicate filter, FilterContext context) {
        return predicateEvaluationService.matchesPermanentPredicate(permanent, filter, context);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return ordered.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}
