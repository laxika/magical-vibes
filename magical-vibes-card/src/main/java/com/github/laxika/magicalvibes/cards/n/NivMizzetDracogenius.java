package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "183")
public class NivMizzetDracogenius extends Card {

    public NivMizzetDracogenius() {
        // Whenever Niv-Mizzet deals damage to a player, you may draw a card.
        // Not limited to combat damage — ON_DAMAGE_TO_PLAYER covers any damage.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new MayEffect(new DrawCardEffect(), "Draw a card?"));

        // {U}{R}: Niv-Mizzet deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(false, "{U}{R}", List.of(new DealDamageToAnyTargetEffect(1)),
                "{U}{R}: Niv-Mizzet deals 1 damage to any target."));
    }
}
