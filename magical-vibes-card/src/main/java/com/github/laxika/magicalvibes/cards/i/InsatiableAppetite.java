package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrElseEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ELD", collectorNumber = "162")
public class InsatiableAppetite extends Card {

    public InsatiableAppetite() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsOrElseEffect(
                new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                1,
                new BoostTargetCreatureEffect(5, 5),
                new BoostTargetCreatureEffect(3, 3),
                "Food"
        ));
    }
}
