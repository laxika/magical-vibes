package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfChosenTypeMustAttackThisTurnEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "180")
public class WalkingDesecration extends Card {

    public WalkingDesecration() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new CreaturesOfChosenTypeMustAttackThisTurnEffect()),
                "{B}, {T}: Creatures of the creature type of your choice attack this turn if able."
        ));
    }
}
