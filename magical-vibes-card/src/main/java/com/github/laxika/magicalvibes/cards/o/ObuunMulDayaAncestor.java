package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNC", collectorNumber = "2")
@CardRegistration(set = "ZNC", collectorNumber = "8")
public class ObuunMulDayaAncestor extends Card {

    public ObuunMulDayaAncestor() {
        target(TargetFilters.landYouControl(), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new AnimatePermanentsEffect(
                        new SourcePower(), new SourcePower(),
                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                        null, Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN,
                        null, Set.of()));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
