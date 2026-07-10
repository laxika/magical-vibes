package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "242")
public class ForceOfNature extends Card {

    public ForceOfNature() {
        // At the beginning of your upkeep, this creature deals 8 damage to you unless you pay {G}{G}{G}{G}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{G}{G}{G}{G}"),
                        List.of(new DealDamageToPlayersEffect(8, DamageRecipient.CONTROLLER)),
                        true));
    }
}
