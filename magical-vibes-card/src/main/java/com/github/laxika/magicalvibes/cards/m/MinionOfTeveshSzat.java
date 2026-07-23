package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "151")
public class MinionOfTeveshSzat extends Card {

    public MinionOfTeveshSzat() {
        // At the beginning of your upkeep, this creature deals 2 damage to you unless you pay {B}{B}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{B}{B}"),
                        List.of(new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER)),
                        true));

        // {T}: Target creature gets +3/-2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(3, -2)),
                "{T}: Target creature gets +3/-2 until end of turn."));
    }
}
