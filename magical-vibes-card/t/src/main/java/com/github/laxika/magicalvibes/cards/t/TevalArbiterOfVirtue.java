package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "230")
public class TevalArbiterOfVirtue extends Card {

    public TevalArbiterOfVirtue() {
        addStaticEffect(new GrantSpellCastingAbilityToSpellsEffect(Keyword.DELVE, null));
        addTrigger(Triggers.youCastSpell(),
                CasterLosesLifeOnSpellCastEffect.equalToSpellManaValue(null));
    }
}
