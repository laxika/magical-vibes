package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.LandsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "129")
public class EmergentSequence extends Card {

    public EmergentSequence() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(1),
                CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED,
                null,
                1,
                false,
                false,
                false,
                new AnimatePermanentsEffect(
                        new Fixed(0),
                        new Fixed(0),
                        List.of(CardSubtype.FRACTAL),
                        Set.of(),
                        null,
                        Set.of(),
                        GrantScope.OWN_PERMANENTS,
                        EffectDuration.PERMANENT,
                        null,
                        Set.of(CardColor.GREEN, CardColor.BLUE))));
        addEffect(EffectSlot.SPELL, new PutCountersOnChosenPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new LandsEnteredBattlefieldThisTurn()));
    }
}
