package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "M12", collectorNumber = "80")
public class VisionsOfBeyond extends Card {

    public VisionsOfBeyond() {
        AnyGraveyardAtLeast twentyCards = new AnyGraveyardAtLeast(20);
        addEffect(EffectSlot.SPELL, new ConditionalEffect(twentyCards, new DrawCardEffect(3)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(twentyCards), new DrawCardEffect(1)));
    }
}
