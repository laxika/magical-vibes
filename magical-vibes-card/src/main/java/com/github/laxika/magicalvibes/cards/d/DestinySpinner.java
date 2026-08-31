package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.EnumSet;
import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "168")
public class DestinySpinner extends Card {

    public DestinySpinner() {
        addEffect(EffectSlot.STATIC, new ControllerSpellsCantBeCounteredEffect(
                EnumSet.of(CardType.CREATURE, CardType.ENCHANTMENT)));

        PermanentCount enchantmentCount = new PermanentCount(
                new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new AnimatePermanentsEffect(
                        enchantmentCount,
                        enchantmentCount,
                        List.of(CardSubtype.ELEMENTAL),
                        EnumSet.of(Keyword.TRAMPLE, Keyword.HASTE),
                        null,
                        EnumSet.noneOf(CardType.class),
                        GrantScope.TARGET,
                        EffectDuration.UNTIL_END_OF_TURN,
                        null)),
                "{3}{G}: Target land you control becomes an X/X Elemental creature with trample and haste until end of turn, where X is the number of enchantments you control. It's still a land.",
                TargetFilters.landYouControl()));
    }
}
