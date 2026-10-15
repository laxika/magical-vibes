package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "HOC", collectorNumber = "11")
public class DragonsDesire extends Card {

    public DragonsDesire() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(
                ManaColor.RED,
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.OPPONENTS)));
    }
}
