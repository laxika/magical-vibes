package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseGameAtEndStepEffect;

@CardRegistration(set = "VOW", collectorNumber = "140")
public class AlchemistsGambit extends Card {

    public AlchemistsGambit() {
        addCastingOption(AlternateHandCast.cleave("{4}{U}{U}{R}", null));
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1, false, true));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()),
                new RegisterLoseGameAtEndStepEffect()));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
