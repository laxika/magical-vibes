package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "314")
public class ChromaticStar extends Card {

    public ChromaticStar() {
        addActivatedAbility(new ActivatedAbility(
                true,                                                            // requiresTap
                "{1}",                                                           // manaCost
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()), // effects
                false,                                                           // needsTarget
                "{1}, {T}, Sacrifice Chromatic Star: Add one mana of any color." // description
        ));
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
