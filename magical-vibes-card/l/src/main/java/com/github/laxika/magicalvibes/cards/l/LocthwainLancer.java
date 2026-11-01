package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "27")
@CardRegistration(set = "MOC", collectorNumber = "114")
public class LocthwainLancer extends Card {

    public LocthwainLancer() {
        SequenceEffect deathEffect = SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new DrawCardEffect(1));

        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.KNIGHT), deathEffect));
    }
}
