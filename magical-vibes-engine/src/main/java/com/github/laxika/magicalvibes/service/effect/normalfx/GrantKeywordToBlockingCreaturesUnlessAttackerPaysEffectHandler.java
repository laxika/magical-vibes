package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves independent attacker-controller payments and grants keywords to matching blockers. */
@Component
@RequiredArgsConstructor
public class GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect) effect;
        if (entry.getTargetId() == null) {
            queuePaymentChoices(gameData, entry, grant);
            return;
        }

        grantKeywordToBlockingCreatures(gameData, entry, grant, entry.getTargetId());
    }

    private void queuePaymentChoices(GameData gameData, StackEntry entry,
                                     GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect effect) {
        Card sourceCard = entry.getCard();
        List<PendingMayAbility> choices = new ArrayList<>();
        for (UUID playerId : apnapOrder(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent attacker : List.copyOf(battlefield)) {
                if (!predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, effect.attackerFilter())) {
                    continue;
                }
                UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
                if (controllerId == null) {
                    continue;
                }
                choices.add(new PendingMayAbility(
                        sourceCard,
                        controllerId,
                        List.of(effect),
                        "Pay " + effect.manaCost() + " to prevent creatures you control blocking "
                                + attacker.getCard().getName() + " from gaining " + formatKeyword(effect.keyword())
                                + "? (" + sourceCard.getName() + ")",
                        attacker.getId(),
                        effect.manaCost(),
                        entry.getSourcePermanentId(),
                        null,
                        0,
                        0,
                        null,
                        null,
                        null,
                        null,
                        entry.getControllerId()
                ));
            }
        }
        if (!choices.isEmpty()) {
            gameData.pendingMayAbilities.addAll(0, choices);
        }
    }

    private void grantKeywordToBlockingCreatures(GameData gameData, StackEntry entry,
                                                   GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect effect,
                                                   UUID attackerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }
        Set<Keyword> keywords = Set.of(effect.keyword());
        GrantKeywordEffect grant = new GrantKeywordEffect(keywords, GrantScope.TARGET);
        for (Permanent blocker : List.copyOf(battlefield)) {
            if (!gameQueryService.isCreature(gameData, blocker)
                    || !blocker.isBlocking()
                    || !blocker.getBlockingTargetIds().contains(attackerId)
                    || gameQueryService.cantHaveOrGainKeyword(gameData, blocker, effect.keyword())) {
                continue;
            }
            blocker.getGrantedKeywords().add(effect.keyword());
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(), grant,
                    blocker.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            gameLogService.append(gameData,
                    GameLog.cardThen(blocker.getCard(), " gains " + formatKeyword(effect.keyword())
                            + " until end of turn."));
        }
    }

    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }

    private static String formatKeyword(Keyword keyword) {
        String name = keyword.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
