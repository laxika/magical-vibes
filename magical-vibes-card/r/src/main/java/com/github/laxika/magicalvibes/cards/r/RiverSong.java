package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawFromBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "152")
@CardRegistration(set = "WHO", collectorNumber = "436")
@CardRegistration(set = "WHO", collectorNumber = "547")
@CardRegistration(set = "WHO", collectorNumber = "757")
@CardRegistration(set = "WHO", collectorNumber = "1027")
@CardRegistration(set = "WHO", collectorNumber = "1138")
public class RiverSong extends Card {

    public RiverSong() {
        addStaticEffect(new DrawFromBottomOfLibraryEffect());

        CardEffect trigger = SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                new DealDamageToPlayersEffect(new SourcePower(), Subjects.targetPlayer()));
        addTrigger(Triggers.opponentScries(), trigger);
        addTrigger(Triggers.opponentSurveils(), trigger);
        addTrigger(Triggers.opponentSearchesTheirLibrary(), trigger);
    }
}
