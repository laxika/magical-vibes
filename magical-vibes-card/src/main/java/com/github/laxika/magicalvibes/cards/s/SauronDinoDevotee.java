package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "731")
public class SauronDinoDevotee extends Card {

    public SauronDinoDevotee() {
        var anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, modal(anotherCreature));
        addEffect(EffectSlot.ON_ATTACK, modal(anotherCreature));
    }

    private ChooseOneAtTriggerTimeEffect modal(PermanentAllOfPredicate anotherCreature) {
        return new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Cure Cancer — You gain 3 life.",
                        new GainLifeEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Turn People into Dinosaurs — Put a saurian counter on another target creature. It's a green Dinosaur with base power and toughness 5/5 for as long as it has a saurian counter on it.",
                        List.of(
                                new GrantEffectsToCounterBearersEffect(CounterType.SAURIAN, List.of(
                                        new GrantColorEffect(CardColor.GREEN, GrantScope.ALL_PERMANENTS, true),
                                        new GrantSubtypeEffect(CardSubtype.DINOSAUR, GrantScope.ALL_PERMANENTS, true),
                                        new SetBasePowerToughnessEffect(5, 5, GrantScope.ALL_CREATURES))),
                                new PutCounterOnTargetPermanentEffect(CounterType.SAURIAN)),
                        new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature"))
        )));
    }
}
