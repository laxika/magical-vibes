package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "26")
public class MirranSpy extends Card {

    public MirranSpy() {
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.TARGET), "Untap target creature?"));
    }
}
