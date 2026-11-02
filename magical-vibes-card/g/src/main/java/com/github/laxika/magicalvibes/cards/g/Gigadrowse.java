package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "26")
public class Gigadrowse extends Card {

    public Gigadrowse() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{U}")));
        target(TargetFilters.permanent())
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.youCastThisSpell(), new ReplicateEffect("{U}"));
    }
}
