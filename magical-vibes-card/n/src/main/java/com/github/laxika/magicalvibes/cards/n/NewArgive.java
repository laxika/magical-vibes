package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;

@CardRegistration(set = "MOC", collectorNumber = "59")
public class NewArgive extends Card {

    public NewArgive() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsHistoricPredicate(),
                        new BoostTargetCreatureEffect(2, 2)));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardIsHistoricPredicate(), LibrarySearchDestination.HAND));
    }
}
