package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCastTriggerToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerGrant;

@CardRegistration(set = "SLD", collectorNumber = "1371")
@CardRegistration(set = "MH1", collectorNumber = "200")
public class TheFirstSliver extends Card {

    public TheFirstSliver() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
        addStaticEffect(new GrantCastTriggerToSpellsEffect(
                SpellCastTriggerGrant.CASCADE_TO_SLIVER_SPELL, new CascadeEffect()));
    }
}
