package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MRD", collectorNumber = "45")
public class Override extends Card {

    public Override() {
        // Counter target spell unless its controller pays {1} for each artifact you control.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
    }
}
