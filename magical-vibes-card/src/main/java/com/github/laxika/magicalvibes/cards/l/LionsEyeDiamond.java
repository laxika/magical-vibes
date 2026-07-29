package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "307")
public class LionsEyeDiamond extends Card {

    public LionsEyeDiamond() {
        // Discard your hand, Sacrifice Lion's Eye Diamond: Add three mana of any one color.
        // Activate only as an instant — instant speed is the engine default, so no timing restriction.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardHandCost(), new SacrificeSelfCost(), new AwardAnyColorManaEffect(3)),
                "Discard your hand, Sacrifice Lion's Eye Diamond: Add three mana of any one color. Activate only as an instant."
        ));
    }
}
