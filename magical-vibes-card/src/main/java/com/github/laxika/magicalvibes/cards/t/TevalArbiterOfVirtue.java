package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;

@CardRegistration(set = "TDM", collectorNumber = "230")
public class TevalArbiterOfVirtue extends Card {

    public TevalArbiterOfVirtue() {
        addEffect(EffectSlot.STATIC,
                new GrantSpellCastingAbilityToSpellsEffect(Keyword.DELVE, null));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                CasterLosesLifeOnSpellCastEffect.equalToSpellManaValue(null));
    }
}
