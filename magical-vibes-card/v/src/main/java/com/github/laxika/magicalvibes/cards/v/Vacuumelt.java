package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "40")
@CardRegistration(set = "DDJ", collectorNumber = "24")
public class Vacuumelt extends Card {

    public Vacuumelt() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{2}{U}")));
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.target());
        addTrigger(Triggers.youCastThisSpell(), new ReplicateEffect("{2}{U}"));
    }
}
