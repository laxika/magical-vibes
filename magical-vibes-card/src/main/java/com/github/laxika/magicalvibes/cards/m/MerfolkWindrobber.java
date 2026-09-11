package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "70")
public class MerfolkWindrobber extends Card {

    public MerfolkWindrobber() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MillEffect(1, MillRecipient.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "Sacrifice Merfolk Windrobber: Draw a card. Activate only if an opponent has eight or more cards in their graveyard."
        ).withActivationCondition(
                new OpponentGraveyardAtLeast(8),
                "Activate only if an opponent has eight or more cards in their graveyard"
        ));
    }
}
