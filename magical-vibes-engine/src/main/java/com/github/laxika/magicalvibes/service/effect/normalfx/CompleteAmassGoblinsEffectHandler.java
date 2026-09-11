package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CompleteAmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompleteAmassGoblinsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CompleteAmassGoblinsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var amass = (CompleteAmassGoblinsEffect) effect;
        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(amass.playerId());
        List<UUID> armyIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(amass.playerId(), List.of())) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, army, filterContext)) {
                armyIds.add(permanent.getId());
            }
        }

        if (armyIds.size() > 1) {
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    amass.playerId(),
                    armyIds,
                    1,
                    new MultiPermanentChoiceContext.OwnPermanentCounterPlacementByPlayerWithChosenReference(
                            CounterType.PLUS_ONE_PLUS_ONE, amass.count(), amass.playerId()),
                    "Choose an Army creature to put " + amass.count() + " +1/+1 counter(s) on.");
            insertFollowUps(entry, effect, true, amass.drawCard());
            return;
        }

        if (armyIds.size() == 1) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, armyIds.getFirst());
            entry.setChosenPermanentId(armyIds.getFirst());
            if (chosen != null) {
                StackEntry placementEntry = new StackEntry(entry);
                placementEntry.setControllerId(amass.playerId());
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, placementEntry, chosen, CounterType.PLUS_ONE_PLUS_ONE, amass.count());
            }
            insertFollowUps(entry, effect, true, amass.drawCard());
            return;
        }

        if (amass.drawCard()) {
            insertFollowUps(entry, effect, false, true);
        }
    }

    private void insertFollowUps(StackEntry entry, CardEffect current, boolean grantGoblinSubtype,
                                 boolean drawCard) {
        List<CardEffect> followUps = new ArrayList<>();
        if (grantGoblinSubtype) {
            followUps.add(new GrantSubtypeToChosenPermanentEffect(CardSubtype.GOBLIN));
        }
        if (drawCard) {
            followUps.add(new DrawCardEffect(1));
        }
        if (followUps.isEmpty()) {
            return;
        }
        int index = entry.getEffectsToResolve().indexOf(current);
        if (index < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(index + 1, followUps);
    }
}
