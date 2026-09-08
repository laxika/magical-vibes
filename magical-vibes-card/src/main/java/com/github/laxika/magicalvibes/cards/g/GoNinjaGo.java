package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "149")
public class GoNinjaGo extends Card {

    public GoNinjaGo() {
        ControlledPermanentPredicateTargetFilter creatureYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control");
        PermanentPredicateTargetFilter creatureOpponentControls = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be a creature an opponent controls");

        CardEffect flicker = FlickerEffect.flickerTarget();
        CardEffect damage = new DealDamageToTargetCreatureEffect(new GreatestPowerAmongControlled());
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature you control, then return it to the battlefield under its owner's control",
                        flicker,
                        creatureYouControl),
                new ChooseOneEffect.ChooseOneOption(
                        "Go Ninja Go deals damage equal to the greatest power among creatures you control to target creature an opponent controls",
                        damage,
                        creatureOpponentControls),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature you control, then return it to the battlefield under its owner's control; Go Ninja Go deals damage equal to the greatest power among creatures you control to target creature an opponent controls",
                        List.of(flicker, damage),
                        List.of(creatureYouControl, creatureOpponentControls))
        )));
    }
}
