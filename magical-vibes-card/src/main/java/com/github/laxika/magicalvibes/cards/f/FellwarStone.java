package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsOpponentLandsCouldProduceEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "297")
public class FellwarStone extends Card {

    public FellwarStone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsOpponentLandsCouldProduceEffect()),
                "{T}: Add one mana of any color that a land an opponent controls could produce."
        ));
    }
}
