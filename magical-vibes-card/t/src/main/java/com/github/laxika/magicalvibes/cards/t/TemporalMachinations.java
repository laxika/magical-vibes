package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "271")
public class TemporalMachinations extends Card {

    public TemporalMachinations() {
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.target());
        addSpellEffect(new ConditionalEffect(new ControlsPermanent(new PermanentIsArtifactPredicate()), new DrawCardEffect(1)));
    }
}
