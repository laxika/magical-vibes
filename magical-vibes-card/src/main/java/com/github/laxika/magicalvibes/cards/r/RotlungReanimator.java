package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "164")
public class RotlungReanimator extends Card {

    public RotlungReanimator() {
        // Whenever this creature or another Cleric dies, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.blackZombie(1));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.CLERIC), CreateTokenEffect.blackZombie(1)));
    }
}
