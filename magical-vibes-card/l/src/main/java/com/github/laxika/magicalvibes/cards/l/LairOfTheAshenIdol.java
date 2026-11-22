package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "24")
@CardRegistration(set = "OPCA", collectorNumber = "46")
public class LairOfTheAshenIdol extends Card {

    public LairOfTheAshenIdol() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ForcedCostOrElseEffect(
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "a creature"),
                List.of(new PlaneswalkEffect())));
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"), 0, 99).addTrigger(Triggers.chaosEnsues(),
                new EachPlayerCreatesTokenEffect(
                        CreateTokenEffect.blackZombie(1), Subjects.targetPlayerDeclaredElsewhere()));
    }
}
