package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

/**
 * Lost Vale — back face of Dowsing Dagger.
 * Land.
 * (Transforms from Dowsing Dagger.)
 * {T}: Add three mana of any one color.
 */
public class LostVale extends Card {

    public LostVale() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(3)),
                "{T}: Add three mana of any one color."
        ));
    }
}
