package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BFZ", collectorNumber = "99")
public class SmotheringAbomination extends Card {

    public SmotheringAbomination() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.CREATURE), new DrawCardEffect()));
    }
}
