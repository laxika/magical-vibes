package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "HBG", collectorNumber = "58")
public class KardumPatronOfFlames extends Card {

    public KardumPatronOfFlames() {
        addEffect(EffectSlot.ON_ATTACK, new PutCountersOnSelfEffect(CounterType.FLAME));
        addEffect(EffectSlot.ON_ATTACK, new SeekLibraryEffect(
                new Fixed(1), new CardTruePredicate(), LibrarySearchDestination.EXILE_WITH_SOURCE,
                new ManaValueBound(new CountersOnSource(CounterType.FLAME), true, 0), true));
        addEffect(EffectSlot.ON_DEATH,
                new PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect());
    }
}
