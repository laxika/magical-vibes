package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "151")
@CardRegistration(set = "ME4", collectorNumber = "91")
public class MinionOfTeveshSzat extends Card {

    public MinionOfTeveshSzat() {
        // At the beginning of your upkeep, this creature deals 2 damage to you unless you pay {B}{B}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{B}{B}", DealDamageEffect.to(Subjects.you(), 2)));

        // {T}: Target creature gets +3/-2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(BoostEffect.target(3, -2)),
                "{T}: Target creature gets +3/-2 until end of turn."));
    }
}
