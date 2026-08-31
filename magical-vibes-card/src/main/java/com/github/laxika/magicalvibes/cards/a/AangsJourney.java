package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "1")
public class AangsJourney extends Card {

    public AangsJourney() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));

        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND),
                SequenceEffect.of(
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND),
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.SHRINE), LibrarySearchDestination.HAND)
                )
        ));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
