package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "176")
@CardRegistration(set = "V12", collectorNumber = "8")
@CardRegistration(set = "IMA", collectorNumber = "238")
@CardRegistration(set = "ZNE", collectorNumber = "25")
@CardRegistration(set = "EOS", collectorNumber = "17")
@CardRegistration(set = "EOS", collectorNumber = "62")
@CardRegistration(set = "EOS", collectorNumber = "107")
@CardRegistration(set = "EOS", collectorNumber = "152")
public class GroveOfTheBurnwillows extends Card {

    public GroveOfTheBurnwillows() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardAnyColorManaEffect(1, List.of(ManaColor.RED, ManaColor.GREEN)),
                        new GainLifeEffect(new Fixed(1), GainLifeRecipient.OPPONENT)
                ),
                "{T}: Add {R} or {G}. Each opponent gains 1 life."
        ));
    }
}
