package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YLCI", collectorNumber = "16")
public class PiratesLanding extends Card {

    public PiratesLanding() {
        TreasureManaSpentToCast treasureManaSpent = new TreasureManaSpentToCast();
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(treasureManaSpent), new DrawCardEffect(1)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                treasureManaSpent, new SeekEffect(new CardSubtypePredicate(CardSubtype.PIRATE))));
    }
}
