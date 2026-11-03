package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1347")
@CardRegistration(set = "KHM", collectorNumber = "374")
public class ValkyrieHarbinger extends Card {

    public ValkyrieHarbinger() {
        // At the beginning of each end step, if you gained 4 or more life this turn,
        // create a 4/4 white Angel creature token with flying and vigilance.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new GainedLifeThisTurn(4)),
                new CreateTokenEffect("Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of()));
    }
}
