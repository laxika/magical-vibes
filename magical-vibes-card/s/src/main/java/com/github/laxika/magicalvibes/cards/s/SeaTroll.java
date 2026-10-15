package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.SourceBlockedOrWasBlockedByColorThisTurn;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "39")
public class SeaTroll extends Card {

    public SeaTroll() {
        // {U}: Regenerate this creature. Activate only if this creature blocked or was blocked by a
        // blue creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new RegenerateEffect()),
                "{U}: Regenerate this creature. Activate only if this creature blocked or was blocked "
                        + "by a blue creature this turn."
        ).withActivationCondition(
                new SourceBlockedOrWasBlockedByColorThisTurn(CardColor.BLUE),
                "Activate only if this creature blocked or was blocked by a blue creature this turn"));
    }
}
