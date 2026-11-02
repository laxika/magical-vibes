package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNC", collectorNumber = "2")
@CardRegistration(set = "ZNC", collectorNumber = "8")
public class ObuunMulDayaAncestor extends Card {

    public ObuunMulDayaAncestor() {
        target(TargetFilters.landYouControl(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new AnimatePermanentsEffect(
                        new SourcePower(), new SourcePower(),
                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                        null, Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN,
                        null, Set.of()));

        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
