package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "164")
public class RotlungReanimator extends Card {

    public RotlungReanimator() {
        // Whenever this creature or another Cleric dies, create a 2/2 black Zombie creature token.
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.blackZombie(1));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.CLERIC), CreateTokenEffect.blackZombie(1)));
    }
}
