package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "106")
public class BatteredGolem extends Card {

    public BatteredGolem() {
        // This creature doesn't untap during your untap step.
        addStaticEffect(DoesntUntapEffect.self());

        // Whenever an artifact enters, you may untap this creature.
        addTrigger(Triggers.enters(new PermanentIsArtifactPredicate(), ObjectRelation.ANY),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF), "Untap this creature?"));
    }
}
