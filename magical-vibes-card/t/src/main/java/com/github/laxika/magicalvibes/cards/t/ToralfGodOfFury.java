package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "154")
@CardRegistration(set = "KHM", collectorNumber = "313")
public class ToralfGodOfFury extends Card {

    public ToralfGodOfFury() {
        setBackFaceCard(new ToralfHammer());
        setModalDoubleFaced(true);

        // "Any target other than that permanent": the permanent dealt the excess damage.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsTriggeringPermanentPredicate()),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be other than the permanent dealt excess damage."))
                .addTrigger(Triggers.creatureOrPlaneswalkerOpponentControlsDealtExcessNoncombatDamage(),
                        new DealDamageToAnyTargetEffect(new EventValue()));
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Toralf, God of Fury", List.of())
                        .withManaCost("{2}{R}{R}"),
                new ChooseOneEffect.ChooseOneOption("Toralf's Hammer", List.of())
                        .withManaCost("{1}{R}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "ToralfHammer";
    }
}
