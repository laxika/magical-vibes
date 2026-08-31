package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "119")
public class SwarmCuller extends Card {

    public SwarmCuller() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        new MayEffect(
                                new SacrificePermanentThenEffect(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentAnyOfPredicate(List.of(
                                                        new PermanentIsCreaturePredicate(),
                                                        new PermanentIsArtifactPredicate()
                                                )),
                                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                                        )),
                                        new DrawCardEffect(1),
                                        "another creature or artifact"),
                                "Sacrifice another creature or artifact?")));
    }
}
