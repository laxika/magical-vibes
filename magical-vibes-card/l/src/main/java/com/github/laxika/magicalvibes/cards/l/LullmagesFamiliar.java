package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "227")
public class LullmagesFamiliar extends Card {

    public LullmagesFamiliar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {G} or {U}."
        ));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new KickedSpellCastTriggerEffect(List.of(new GainLifeEffect(2))));
    }
}
