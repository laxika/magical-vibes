package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/**
 * Itlimoc, Cradle of the Sun — back face of Growing Rites of Itlimoc.
 * Legendary Land.
 * {T}: Add {G}.
 * {T}: Add {G} for each creature you control.
 */
public class ItlimocCradleOfTheSun extends Card {

    public ItlimocCradleOfTheSun() {
        // {T}: Add {G}.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));

        // {T}: Add {G} for each creature you control.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AddManaPerControlledPermanentEffect(
                        ManaColor.GREEN,
                        new PermanentIsCreaturePredicate(),
                        "creatures"
                )),
                "{T}: Add {G} for each creature you control."
        ));
    }
}
