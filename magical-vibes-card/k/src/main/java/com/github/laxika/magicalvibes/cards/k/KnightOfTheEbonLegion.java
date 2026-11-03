package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "105")
public class KnightOfTheEbonLegion extends Card {

    public KnightOfTheEbonLegion() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new BoostSelfEffect(3, 3),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)
                ),
                "{2}{B}: This creature gets +3/+3 and gains deathtouch until end of turn."
        ));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new AnyPlayerLostLifeThisTurn(4)),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
