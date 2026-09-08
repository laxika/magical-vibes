package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "57")
public class SnowmeltStag extends Card {

    public SnowmeltStag() {
        // During your turn, this creature has base power and toughness 5/2.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new SetBasePowerToughnessEffect(5, 2, GrantScope.SELF)));

        // {5}{U}{U}: This creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{5}{U}{U}: This creature can't be blocked this turn."
        ));
    }
}
