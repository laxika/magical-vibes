package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "125")
public class AnaxHardenedInTheForge extends Card {

    private static final CreateTokenEffect SATYR_TOKEN = new CreateTokenEffect(
            new FixedIfCondition(new EventValueAtLeast(4), 2, 1), "Satyr", 1, 1,
            CardColor.RED, List.of(CardSubtype.SATYR), Set.of(), Set.of())
            .withTokenEffects(Map.of(EffectSlot.STATIC, new CantBlockEffect()));

    public AnaxHardenedInTheForge() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.RED), new Fixed(3)));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))), SATYR_TOKEN));
        addEffect(EffectSlot.ON_DEATH, SATYR_TOKEN);
    }
}
