package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "78")
@CardRegistration(set = "ATH", collectorNumber = "47")
@CardRegistration(set = "ME2", collectorNumber = "147")
@CardRegistration(set = "DDL", collectorNumber = "32")
@CardRegistration(set = "EMA", collectorNumber = "143")
@CardRegistration(set = "SLZ", collectorNumber = "67")
@CardRegistration(set = "SLZ", collectorNumber = "188")
@CardRegistration(set = "SLZ", collectorNumber = "309")
public class Pyrokinesis extends Card {

    public Pyrokinesis() {
        // You may exile a red card from your hand rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.RED), "red"))));

        // Pyrokinesis deals 4 damage divided as you choose among any number of target creatures.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(4));
    }
}
