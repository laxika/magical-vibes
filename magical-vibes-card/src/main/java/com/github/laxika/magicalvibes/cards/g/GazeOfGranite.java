package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Gaze of Granite — {X}{B}{B}{G} Sorcery.
 * Destroy each nonland permanent with mana value X or less.
 */
@CardRegistration(set = "DGM", collectorNumber = "72")
public class GazeOfGranite extends Card {

    public GazeOfGranite() {
        // The X paid on cast bounds the wipe; tokens have mana value 0 and are always caught.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentManaValueAtMostXPredicate()))));
    }
}
