package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "42")
@CardRegistration(set = "TDC", collectorNumber = "82")
public class ArborAdherent extends Card {

    public ArborAdherent() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(new GreatestToughnessAmongControlled(otherCreatures))),
                "{T}: Add X mana of any one color, where X is the greatest toughness among other creatures you control."
        ));
    }
}
