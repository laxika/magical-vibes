package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "62")
@CardRegistration(set = "AA4", collectorNumber = "16")
public class BringerOfTheRedDawn extends Card {

    public BringerOfTheRedDawn() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}{U}{B}{R}{G}"))));

        target(TargetFilters.creature()).addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(new MayEffect(
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.TARGET),
                                new GainControlOfTargetEffect(ControlDuration.END_OF_TURN)
                        ),
                        "Untap target creature and gain control of it until end of turn?"
                ), new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)));
    }
}
