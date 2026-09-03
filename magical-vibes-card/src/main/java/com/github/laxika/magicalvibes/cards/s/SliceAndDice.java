package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "232")
public class SliceAndDice extends Card {

    public SliceAndDice() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(4));

        addHandActivatedAbility(new ActivatedAbility(false, "{2}{R}",
                List.of(
                        new MayEffect(new MassDamageEffect(1),
                                "Have Slice and Dice deal 1 damage to each creature?"),
                        new DrawCardEffect(1)),
                "Cycling {2}{R} ({2}{R}, Discard this card: Draw a card.)"));
    }
}
