package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "9")
public class CosmograndZenith extends Card {

    private static final String TOKEN_MODE = "Create two 1/1 white Human Soldier creature tokens.";
    private static final String COUNTER_MODE = "Put a +1/+1 counter on each creature you control.";

    public CosmograndZenith() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                2,
                null,
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(TOKEN_MODE,
                                new CreateTokenEffect(2, "Human Soldier", 1, 1,
                                        CardColor.WHITE,
                                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                        Set.of(), Set.of())),
                        new ChooseOneEffect.ChooseOneOption(COUNTER_MODE,
                                new PutCounterOnEachControlledPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE,
                                        1,
                                        new PermanentIsCreaturePredicate()))
                )))
        ));
    }
}
