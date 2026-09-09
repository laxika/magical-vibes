package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "14")
public class TitansPresence extends Card {

    public TitansPresence() {
        addEffect(EffectSlot.SPELL, new RevealCardFromHandCost(
                new CardAllOfPredicate(List.of(
                        new CardIsColorlessPredicate(),
                        new CardTypePredicate(CardType.CREATURE)
                )),
                "colorless creature", false, true));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentPowerAtMostXPredicate()),
                        new ExileTargetPermanentEffect()));
    }
}
