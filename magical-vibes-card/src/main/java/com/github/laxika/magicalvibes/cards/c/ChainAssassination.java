package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.AnotherCreatureDiedThisTurn;
import com.github.laxika.magicalvibes.model.condition.Freerunning;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "23")
public class ChainAssassination extends Card {

    public ChainAssassination() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{B}")), new Freerunning(), false));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                new ConditionalEffect(new AnotherCreatureDiedThisTurn(), new DrawCardEffect()),
                ThenEffectRecipient.CONTROLLER));
    }
}
