package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "17")
public class HallowedHaunting extends Card {

    public HallowedHaunting() {
        // As long as you control seven or more enchantments, creatures you control have flying and vigilance.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(7, new PermanentIsEnchantmentPredicate()),
                new StaticBoostEffect(0, 0, Set.of(Keyword.FLYING, Keyword.VIGILANCE), GrantScope.OWN_CREATURES)));

        // Whenever you cast an enchantment spell, create a white Spirit Cleric token whose P/T
        // are each equal to the number of Spirits you control.
        PermanentCount spirits = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT), CountScope.CONTROLLER);
        CreateTokenEffect spiritCleric = new CreateTokenEffect(
                1, "Spirit Cleric", 0, 0, CardColor.WHITE,
                List.of(CardSubtype.SPIRIT, CardSubtype.CLERIC), Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(spirits, spirits)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.ENCHANTMENT),
                List.of(spiritCleric)));
    }
}
