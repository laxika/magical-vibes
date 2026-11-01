package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCastTriggerToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerGrant;

@CardRegistration(set = "NCC", collectorNumber = "50")
@CardRegistration(set = "NCC", collectorNumber = "150")
@CardRegistration(set = "OTC", collectorNumber = "177")
@CardRegistration(set = "BLC", collectorNumber = "200")
public class RainOfRiches extends Card {

    public RainOfRiches() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(2));
        addStaticEffect(new GrantCastTriggerToSpellsEffect(
                SpellCastTriggerGrant.CASCADE_TO_FIRST_SPELL_USING_TREASURE_MANA, new CascadeEffect()));
    }
}
