package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageEachDestroyedPermanentControllerUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Stench of Evil — "Destroy all Plains. For each land destroyed this way, Stench of Evil deals 1
 * damage to that land's controller unless they pay {2}."
 *
 * <p>The rider is per destroyed land, not per player: a player who lost three Plains gets three
 * separate {2} decisions.
 */
@CardRegistration(set = "ICE", collectorNumber = "165")
public class StenchOfEvil extends Card {

    public StenchOfEvil() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS),
                new DamageEachDestroyedPermanentControllerUnlessPaysEffect(1, "{2}")));
    }
}
