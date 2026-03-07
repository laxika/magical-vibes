package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "NPH", collectorNumber = "20")
public class PuresteelPaladin extends Card {

    public PuresteelPaladin() {
        addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));

        addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(
                new GrantActivatedAbilityEffect(
                        new EquipActivatedAbility("{0}"),
                        GrantScope.OWN_PERMANENTS,
                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)
                )
        ));
    }
}
