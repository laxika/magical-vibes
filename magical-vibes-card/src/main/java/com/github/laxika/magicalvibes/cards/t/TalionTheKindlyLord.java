package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePowerOrToughnessEqualsSourceChosenNumberPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "215")
public class TalionTheKindlyLord extends Card {

    public TalionTheKindlyLord() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseNumberOnEnterEffect(1, 10));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER), new DrawCardEffect(1)),
                new StackEntryManaValuePowerOrToughnessEqualsSourceChosenNumberPredicate()));
    }
}
