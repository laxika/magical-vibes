package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSourceFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SUM", collectorNumber = "166")
@CardRegistration(set = "3ED", collectorNumber = "166")
@CardRegistration(set = "ME4", collectorNumber = "127")
@CardRegistration(set = "ARN", collectorNumber = "42")
public class MijaeDjinn extends Card {

    public MijaeDjinn() {
        addTrigger(Triggers.attacks(), new FlipCoinWinEffect(null,
                SequenceEffect.of(
                        new RemoveSourceFromCombatEffect(),
                        new TapPermanentsEffect(TapUntapScope.SELF))));
    }
}
