package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "203")
@CardRegistration(set = "C17", collectorNumber = "212")
public class GrapplingHook extends Card {

    public GrapplingHook() {
        addStaticEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.attacks(),
                new MayEffect(MustBlockSourceEffect.ofTriggeringPermanent(),
                        "Have target creature block it this turn if able?"));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
