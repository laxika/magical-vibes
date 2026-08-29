package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "201")
public class MaskOfTheJadecrafter extends Card {

    public MaskOfTheJadecrafter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect("Golem", new XValue(), new XValue(), null,
                                List.of(CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{X}, {T}, Sacrifice this artifact: Create an X/X colorless Golem artifact creature token. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addUnearth("{2}{G}");
    }
}
