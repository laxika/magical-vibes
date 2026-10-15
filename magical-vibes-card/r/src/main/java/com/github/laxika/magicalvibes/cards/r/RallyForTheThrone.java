package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "25")
public class RallyForTheThrone extends Card {

    public RallyForTheThrone() {
        // Create two 1/1 white Human creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Human", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()));

        // Adamant — If at least three white mana was spent to cast this spell, you gain 1 life
        // for each creature you control.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.WHITE, 3),
                new GainLifeEffect(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))));
    }
}
