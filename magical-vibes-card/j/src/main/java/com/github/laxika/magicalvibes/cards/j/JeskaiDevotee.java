package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "110")
public class JeskaiDevotee extends Card {

    public JeskaiDevotee() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(new BoostSelfEffect(1, 1))
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.RED, ManaColor.WHITE))),
                "{1}: Add {U}, {R}, or {W}. Activate only once each turn.",
                1
        ));
    }
}
