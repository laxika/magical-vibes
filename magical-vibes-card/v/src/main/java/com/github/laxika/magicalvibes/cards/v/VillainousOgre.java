package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "148")
public class VillainousOgre extends Card {

    public VillainousOgre() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        // The regenerate ability exists only while you control a Demon; modelled as an activation gate.
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                "{B}: Regenerate this creature.", CardSubtype.DEMON, 1));
    }
}
