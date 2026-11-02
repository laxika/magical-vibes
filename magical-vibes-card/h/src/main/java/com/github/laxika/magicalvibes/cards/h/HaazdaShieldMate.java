package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "12")
public class HaazdaShieldMate extends Card {

    public HaazdaShieldMate() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ForcedCostOrElseEffect(
                new PayManaCost("{W}{W}"),
                List.of(new SacrificeSelfEffect()),
                true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYou()),
                "The next time a source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
