package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;

@CardRegistration(set = "PIP", collectorNumber = "81")
@CardRegistration(set = "PIP", collectorNumber = "400")
@CardRegistration(set = "PIP", collectorNumber = "609")
@CardRegistration(set = "PIP", collectorNumber = "928")
public class PowerFist extends Card {

    public PowerFist() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER,
                new PutCounterOnReferencedPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
