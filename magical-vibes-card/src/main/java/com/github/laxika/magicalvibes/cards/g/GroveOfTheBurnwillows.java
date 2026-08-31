package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "176")
public class GroveOfTheBurnwillows extends Card {

    public GroveOfTheBurnwillows() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.RED),
                        new GainLifeEffect(new Fixed(1), GainLifeRecipient.OPPONENT)
                ),
                "{T}: Add {R}. Each opponent gains 1 life."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.GREEN),
                        new GainLifeEffect(new Fixed(1), GainLifeRecipient.OPPONENT)
                ),
                "{T}: Add {G}. Each opponent gains 1 life."
        ));
    }
}
