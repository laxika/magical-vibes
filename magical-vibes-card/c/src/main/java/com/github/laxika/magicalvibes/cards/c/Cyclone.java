package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "62")
@CardRegistration(set = "ME4", collectorNumber = "148")
@CardRegistration(set = "ARN", collectorNumber = "45")
public class Cyclone extends Card {

    public Cyclone() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.WIND),
                // then sacrifice it unless you pay {G} for each wind counter on it; if you pay, it deals that much
                // damage to each creature and each player.
                new UnlessPaysEffect(Subjects.you(), PayManaCost.perAmount("{G}", new CountersOnSource(CounterType.WIND)),
                        new SacrificeSelfEffect(),
                        new MassDamageEffect(new CountersOnSource(CounterType.WIND), true))));
    }
}
