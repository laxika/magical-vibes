package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "162")
public class OrzhovaTheChurchOfDeals extends Card {

    public OrzhovaTheChurchOfDeals() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}{B}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(1)),
                "{3}{W}{B}, {T}: Target player loses 1 life and you gain 1 life."
        ));
    }
}
