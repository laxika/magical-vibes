package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "DRB", collectorNumber = "11")
public class NivMizzetTheFiremind extends Card {

    public NivMizzetTheFiremind() {
        // Whenever you draw a card, Niv-Mizzet deals 1 damage to any target.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new DealDamageToAnyTargetEffect(1));

        // {T}: Draw a card.
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DrawCardEffect(1)),
                "{T}: Draw a card."));
    }
}
