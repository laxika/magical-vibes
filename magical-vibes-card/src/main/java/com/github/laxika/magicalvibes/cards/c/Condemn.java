package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.AttackingTargetFilter;

public class Condemn extends Card {

    public Condemn() {
        setNeedsTarget(true);
        setTargetFilter(new AttackingTargetFilter());
        addEffect(EffectSlot.SPELL, new GainLifeEqualToTargetToughnessEffect());
        addEffect(EffectSlot.SPELL, new PutTargetOnBottomOfLibraryEffect());
    }
}
