package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Queues Heroism-style independent payment choices for the matching attackers. */
@Component
@RequiredArgsConstructor
public class PreventCombatDamageFromAttackingCreaturesUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect) effect;
        Card sourceCard = entry.getCard();
        List<PendingMayAbility> choices = new ArrayList<>();

        for (UUID playerId : apnapOrder(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent attacker : List.copyOf(battlefield)) {
                if (!predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, e.attackerFilter())) {
                    continue;
                }
                UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
                if (controllerId == null) {
                    continue;
                }
                choices.add(new PendingMayAbility(
                        sourceCard,
                        controllerId,
                        List.of(e),
                        "Pay " + e.manaCost() + " to have " + attacker.getCard().getName()
                                + " deal combat damage this turn? (" + sourceCard.getName() + ")",
                        attacker.getId(),
                        e.manaCost(),
                        entry.getSourcePermanentId()
                ));
            }
        }

        if (!choices.isEmpty()) {
            gameData.pendingMayAbilities.addAll(0, choices);
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
}
