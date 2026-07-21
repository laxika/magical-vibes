package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Awoken Horror — back face of Thing in the Ice // Awoken Horror.
 * When this creature transforms into Awoken Horror, return all non-Horror creatures
 * to their owners' hands.
 */
public class AwokenHorror extends Card {

    public AwokenHorror() {
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HORROR))))));
    }
}
