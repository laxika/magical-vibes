package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;

@CardRegistration(set = "ECL", collectorNumber = "145")
public class HexingSquelcher extends Card {

    public HexingSquelcher() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new ControllerSpellsCantBeCounteredEffect());
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(0, 2));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(0, 2),
                GrantScope.OWN_CREATURES));
    }
}
