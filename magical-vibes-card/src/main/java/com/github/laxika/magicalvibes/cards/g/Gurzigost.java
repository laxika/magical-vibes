package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "126")
public class Gurzigost extends Card {

    public Gurzigost() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new PutCardsFromGraveyardOnBottomOfLibraryCost(2),
                List.of(new SacrificeSelfEffect()),
                true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new MayEffect(
                                new GrantEffectToSourceUntilEndOfTurnEffect(
                                        EffectSlot.STATIC,
                                        new AssignCombatDamageAsThoughUnblockedEffect()),
                                "Have Gurzigost assign its combat damage as though it weren't blocked?")),
                "{G}{G}, Discard a card: You may have Gurzigost assign its combat damage this turn as "
                        + "though it weren't blocked."
        ));
    }
}
