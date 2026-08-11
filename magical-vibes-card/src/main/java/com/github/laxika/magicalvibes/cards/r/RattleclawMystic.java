package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "144")
public class RattleclawMystic extends Card {

    public RattleclawMystic() {
        addMorph("{2}");
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED))),
                "{T}: Add {G}, {U}, or {R}."
        ));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new AwardManaEffect(ManaColor.GREEN));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new AwardManaEffect(ManaColor.BLUE));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new AwardManaEffect(ManaColor.RED));
    }
}
