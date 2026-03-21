package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllOpponentsGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Phyrexian Scriptures — {2}{B}{B} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I — Put a +1/+1 counter on up to one target creature. That creature becomes an artifact
 *     in addition to its other types.
 * II — Destroy all nonartifact creatures.
 * III — Exile all opponents' graveyards.
 */
@CardRegistration(set = "DOM", collectorNumber = "100")
public class PhyrexianScriptures extends Card {

    public PhyrexianScriptures() {
        // Chapter I: Put a +1/+1 counter on up to one target creature.
        // That creature becomes an artifact in addition to its other types (permanently).
        addEffect(EffectSlot.SAGA_CHAPTER_I, new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new AddCardTypeToTargetPermanentEffect(CardType.ARTIFACT, true));

        // Chapter II: Destroy all nonartifact creatures
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate())
                ))
        ));

        // Chapter III: Exile all opponents' graveyards
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileAllOpponentsGraveyardsEffect());
    }
}
