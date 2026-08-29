package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "JUD", collectorNumber = "115")
public class FolkMedicine extends Card {

    public FolkMedicine() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
        addCastingOption(new FlashbackCast("{1}{W}"));
    }
}
