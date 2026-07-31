package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "82a")
@CardRegistration(set = "ALL", collectorNumber = "82b")
public class VarchildsCrusader extends Card {

    public VarchildsCrusader() {
        // {0}: This creature can't be blocked this turn except by Walls.
        // Sacrifice this creature at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(
                        new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.WALL), "Walls", true),
                        new SacrificeSelfAtEndStepEffect()),
                "{0}: This creature can't be blocked this turn except by Walls. Sacrifice this creature at the beginning of the next end step."));
    }
}
