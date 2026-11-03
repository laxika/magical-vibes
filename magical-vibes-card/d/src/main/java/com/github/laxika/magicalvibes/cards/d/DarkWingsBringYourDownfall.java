package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.CreatureDeathsThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceDownCommandZoneCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "332")
public class DarkWingsBringYourDownfall extends Card {

    public DarkWingsBringYourDownfall() {
        addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.COMMAND), TriggerOptions.DEFAULT,
                new CreateTokenEffect(1, "Demon", 5, 5, CardColor.BLACK,
                        List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), true, false));

        addTrigger(new TriggerCondition(Triggers.endStep(ActivePlayer.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT.withInterveningIf(new CreatureDeathsThisTurnAtLeast(2, CountScope.CONTROLLER)),
                new TurnFaceDownCommandZoneCardEffect());
    }
}
