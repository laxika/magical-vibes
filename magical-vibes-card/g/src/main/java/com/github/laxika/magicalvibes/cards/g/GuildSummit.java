package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsThenDrawPerTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "41")
@CardRegistration(set = "PIO", collectorNumber = "56")
public class GuildSummit extends Card {

    public GuildSummit() {
        // When this enchantment enters, you may tap any number of untapped Gates you control.
        // Draw a card for each Gate tapped this way.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new TapAnyNumberOfPermanentsThenDrawPerTappedEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.GATE)));

        // Whenever a Gate you control enters, draw a card.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.LAND,
                                new PermanentHasSubtypePredicate(CardSubtype.GATE))),
                        ObjectRelation.YOU_CONTROL),
                new DrawCardEffect());
    }
}
