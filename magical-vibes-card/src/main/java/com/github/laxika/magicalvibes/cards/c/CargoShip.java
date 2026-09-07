package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "47")
public class CargoShip extends Card {

    public CargoShip() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 1, new ManaRestriction.ArtifactSpells())),
                "{T}: Add {C}. Spend this mana only to cast an artifact spell or activate an ability of an artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
