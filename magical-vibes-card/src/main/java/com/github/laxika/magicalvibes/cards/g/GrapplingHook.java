package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;

@CardRegistration(set = "ZEN", collectorNumber = "203")
public class GrapplingHook extends Card {

    public GrapplingHook() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                new MayEffect(new MustBlockSourceEffect(null),
                        "Have target creature block it this turn if able?"),
                GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
