package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "C14", collectorNumber = "3")
public class BenevolentOffering extends Card {

    public BenevolentOffering() {
        CreateTokenEffect spirits = CreateTokenEffect.whiteSpirit(3);
        addEffect(EffectSlot.SPELL, spirits);
        addEffect(EffectSlot.SPELL, new EachOpponentCreatesTokenEffect(spirits));

        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER), 2)));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS), 2),
                GainLifeRecipient.OPPONENT));
    }
}
