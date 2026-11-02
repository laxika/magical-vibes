package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawFromBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
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
                new PutCountersOnSourceEffect(1, 1, 1),
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.TARGET_PLAYER));
        addTrigger(Triggers.opponentScries(), trigger);
        addTrigger(Triggers.opponentSurveils(), trigger);
        addTrigger(Triggers.opponentSearchesTheirLibrary(), trigger);
    }
}
