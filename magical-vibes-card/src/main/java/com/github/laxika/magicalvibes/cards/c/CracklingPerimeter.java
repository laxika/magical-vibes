package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "88")
public class CracklingPerimeter extends Card {

    public CracklingPerimeter() {
        // Tap an untapped Gate you control: This enchantment deals 1 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.GATE)),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "Tap an untapped Gate you control: This enchantment deals 1 damage to each opponent."));
    }
}
