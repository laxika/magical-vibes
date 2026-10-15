package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "FIN", collectorNumber = "8")
public class AuronsInspiration extends Card {

    public AuronsInspiration() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(2, 0, new PermanentIsAttackingPredicate()));
        addCastingOption(new FlashbackCast("{2}{W}{W}"));
    }
}
