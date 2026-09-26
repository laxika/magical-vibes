package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2308")
@CardRegistration(set = "SOC", collectorNumber = "242")
public class CursedMirror extends Card {

    public CursedMirror() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.RED));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CopyPermanentOnEnterEffect.temporaryCopy(
                new PermanentIsCreaturePredicate(), "creature", Set.of(Keyword.HASTE)));
    }
}
