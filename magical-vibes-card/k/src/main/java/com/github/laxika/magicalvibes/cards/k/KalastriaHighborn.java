package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "59")
public class KalastriaHighborn extends Card {

    private static final UnlessPaysEffect DEATH_TRIGGER = UnlessPaysEffect.youMayPay("{B}", SequenceEffect.of(
                    new LoseLifeEffect(2, Subjects.targetPlayer()),
                    new GainLifeEffect(2)));

    public KalastriaHighborn() {
        // Whenever this creature or another Vampire you control dies, you may pay {B}. If you do,
        // target player loses 2 life and you gain 2 life.
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                DEATH_TRIGGER);
    }
}
