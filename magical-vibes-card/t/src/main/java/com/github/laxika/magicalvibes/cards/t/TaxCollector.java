package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "9")
public class TaxCollector extends Card {

    public TaxCollector() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tax — Until your next turn, spells your opponents cast cost {1} more to cast",
                        new IncreaseSpellCostUntilNextTurnEffect(new CardTruePredicate(), 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Arrest — Detain target creature an opponent controls",
                        new LockTargetPermanentEffect(true, true, true,
                                EffectDuration.UNTIL_YOUR_NEXT_TURN),
                        TargetFilters.creatureAnOpponentControls())
        )));
    }
}
