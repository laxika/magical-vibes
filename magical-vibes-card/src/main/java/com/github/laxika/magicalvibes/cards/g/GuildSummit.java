package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsThenDrawPerTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "GRN", collectorNumber = "41")
public class GuildSummit extends Card {

    public GuildSummit() {
        // When this enchantment enters, you may tap any number of untapped Gates you control.
        // Draw a card for each Gate tapped this way.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new TapAnyNumberOfPermanentsThenDrawPerTappedEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.GATE)));

        // Whenever a Gate you control enters, draw a card.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.GATE),
                        new DrawCardEffect()));
    }
}
