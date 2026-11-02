package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "16")
@CardRegistration(set = "MMA", collectorNumber = "30")
public class StirThePride extends Card {

    public StirThePride() {
        addCastingCost(new EscalateManaCost("{1}{W}"));
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +2/+2 until end of turn",
                        new BoostAllOwnCreaturesEffect(2, 2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, creatures you control gain \"Whenever this creature deals damage, you gain that much life.\"",
                        new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.dealsDamage(),
                                new GainLifeEffect(new EventValue()))))
        )));
    }
}
