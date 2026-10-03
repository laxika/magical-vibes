package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CantWinGameEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantLoseLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromOpponentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "506")
@CardRegistration(set = "LTC", collectorNumber = "550")
public class CourageousResolve extends Card {

    public CourageousResolve() {
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addEffect(EffectSlot.SPELL, new GrantProtectionFromOpponentsUntilEndOfTurnEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerLifeAtMost(5),
                new ControllerCantLoseLifeThisTurnEffect()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerLifeAtMost(5),
                new GrantPlayerStaticEffectsUntilEndOfTurnEffect(List.of(
                        new CantLoseGameEffect(), new CantWinGameEffect()))));
    }
}
