package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "58")
@CardRegistration(set = "DBL", collectorNumber = "325")
public class DreamshackleGeist extends Card {

    public DreamshackleGeist() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target creature.",
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature doesn't untap during its controller's next untap step.",
                        new SkipNextUntapEffect(TapUntapScope.TARGET),
                        TargetFilters.creature())
        ), true));
    }
}
