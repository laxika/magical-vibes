package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1824")
public class DelayedBlastFireball extends Card {

    public DelayedBlastFireball() {
        addSpellEffect(new ConditionalReplacementEffect(
                new CastFromZone(Zone.EXILE),
                damage(2),
                damage(5)
        ));
        addCastingOption(new ForetellCast("{4}{R}{R}"));
    }

    private static SequenceEffect damage(int amount) {
        return SequenceEffect.simultaneous(
                DealDamageEffect.to(Subjects.eachOpponent(), amount),
                DealDamageEffect.toEach(
                        Subjects.controlledBy(Subjects.eachOpponent(), new PermanentIsCreaturePredicate()),
                        amount)
        );
    }
}
