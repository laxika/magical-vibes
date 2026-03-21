package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "144")
public class SkirkProspector extends Card {

    public SkirkProspector() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSubtypeCreatureCost(CardSubtype.GOBLIN), new AwardManaEffect(ManaColor.RED)),
                "Sacrifice a Goblin: Add {R}."
        ));
    }
}
