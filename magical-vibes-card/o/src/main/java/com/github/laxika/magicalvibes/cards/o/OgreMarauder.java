package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "75")
public class OgreMarauder extends Card {

    public OgreMarauder() {
        // Whenever this creature attacks, it gains "this creature can't be blocked" until end of
        // turn unless defending player sacrifices a creature of their choice.
        addEffect(EffectSlot.ON_ATTACK, ForcedCostOrElseEffect.defendingPlayerMayPay(
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice a creature"),
                List.of(new MakeCreatureUnblockableEffect(true))));
    }
}
