package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "NPH", collectorNumber = "26")
public class WarReport extends Card {

    public WarReport() {
        // Creatures and artifacts are counted independently: an artifact creature adds 2 life.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Sum(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER),
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.ANY_PLAYER)
        )));
    }
}
