package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "152")
public class SeasonOfTheBold extends Card {

    public SeasonOfTheBold() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.budgetedModes(List.of(
                new ChooseOneEffect.ChooseOneOption("Create a tapped Treasure token",
                        CreateTokenEffect.ofTreasureToken(1, true)),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Until the end of your next turn, you may play them.",
                        new ExileTopCardsMayPlayUntilNextTurnEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Until the end of your next turn, whenever you cast a spell, deal 2 damage to up to one target creature.",
                        new RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(
                                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                                new DealDamageToTargetCreatureEffect(2),
                                TargetFilters.creature()))
        ), List.of(1, 2, 3), 5));
    }
}
