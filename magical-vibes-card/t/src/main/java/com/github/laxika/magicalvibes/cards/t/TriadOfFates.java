package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "206")
public class TriadOfFates extends Card {

    public TriadOfFates() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        PermanentPredicate fateCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasCountersPredicate(CounterType.FATE)
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.FATE, 1, anotherCreature)),
                "{1}, {T}: Put a fate counter on another target creature.",
                new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature")));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(FlickerEffect.flickerTarget()),
                "{W}, {T}: Exile target creature that has a fate counter on it, then return it to the battlefield under its owner's control.",
                new PermanentPredicateTargetFilter(fateCreature, "Target must be a creature with a fate counter on it")));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new ExileTargetPermanentThenEffect(
                        new DrawCardEffect(2), ThenEffectRecipient.TARGET_CONTROLLER)),
                "{B}, {T}: Exile target creature that has a fate counter on it. Its controller draws two cards.",
                new PermanentPredicateTargetFilter(fateCreature, "Target must be a creature with a fate counter on it")));
    }
}
