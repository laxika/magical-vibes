package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "103")
public class BloodSpeaker extends Card {

    public BloodSpeaker() {
        // At the beginning of your upkeep, you may sacrifice this creature. If you do, search your
        // library for a Demon card, reveal that card, put it into your hand, then shuffle.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DEMON))),
                        "Sacrifice Blood Speaker to search your library for a Demon card?"));

        // Whenever a Demon you control enters, return this card from your graveyard to your hand.
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DEMON),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()));
    }
}
