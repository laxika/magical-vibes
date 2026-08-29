package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaToChosenPlayerEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "271")
public class SpectralSearchlight extends Card {

    public SpectralSearchlight() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AwardManaToChosenPlayerEffect.anyColor(1)),
                "{T}: Choose a player. That player adds one mana of any color they choose."
        ));
    }
}
