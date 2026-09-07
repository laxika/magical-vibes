package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "232")
public class TheLionTurtle extends Card {

    public TheLionTurtle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new GraveyardCardThreshold(3, new CardSubtypePredicate(CardSubtype.LESSON)),
                "there are three or more Lesson cards in your graveyard"
        ));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
