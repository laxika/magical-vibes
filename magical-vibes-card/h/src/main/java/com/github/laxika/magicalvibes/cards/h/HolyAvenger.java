package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoEquippedCreatureEffect;

@CardRegistration(set = "AFC", collectorNumber = "6")
@CardRegistration(set = "AFC", collectorNumber = "275")
public class HolyAvenger extends Card {

    public HolyAvenger() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE,
                new MayEffect(
                        new PutAuraFromHandOntoEquippedCreatureEffect(),
                        "Put an Aura card from your hand onto the battlefield attached to the equipped creature?"));
        addActivatedAbility(new EquipActivatedAbility("{2}{W}"));
    }
}
