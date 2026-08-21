package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "17")
public class SpectralLynx extends Card {

    public SpectralLynx() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                "{B}: Regenerate this creature."));
    }
}
