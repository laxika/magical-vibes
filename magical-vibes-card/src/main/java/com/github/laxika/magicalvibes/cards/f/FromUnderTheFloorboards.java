package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastForMadnessCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "111")
public class FromUnderTheFloorboards extends Card {

    public FromUnderTheFloorboards() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForMadnessCost()),
                tappedBlackZombie(new Fixed(3))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForMadnessCost()),
                new GainLifeEffect(3)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForMadnessCost(),
                tappedBlackZombie(new XValue())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForMadnessCost(),
                new GainLifeEffect(new XValue())));

        addCastingOption(new MadnessCast("{X}{B}{B}"));
    }

    private static CreateTokenEffect tappedBlackZombie(DynamicAmount amount) {
        return new CreateTokenEffect(
                CardType.CREATURE, amount, "Zombie", 2, 2, CardColor.BLACK, null,
                List.of(CardSubtype.ZOMBIE), Set.<Keyword>of(), Set.<CardType>of(), false, true,
                Map.of(), List.of(), false, false, false, 0, Set.of());
    }
}
