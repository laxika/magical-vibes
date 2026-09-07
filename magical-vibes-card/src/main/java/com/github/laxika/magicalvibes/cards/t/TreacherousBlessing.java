package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "THB", collectorNumber = "117")
public class TreacherousBlessing extends Card {

    public TreacherousBlessing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(3));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CasterLosesLifeOnSpellCastEffect(null, 1));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new SacrificeSelfEffect());
    }
}
