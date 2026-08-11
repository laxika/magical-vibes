package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "151")
@CardRegistration(set = "INV", collectorNumber = "299")
public class ChromaticSphere extends Card {

    public ChromaticSphere() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect(), new DrawCardEffect()),
                "{1}, {T}, Sacrifice Chromatic Sphere: Add one mana of any color. Draw a card."
        ));
    }
}
