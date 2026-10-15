package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "207")
public class Pulverize extends Card {

    public Pulverize() {
        // You may sacrifice two Mountains rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new SacrificePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)))));

        // Destroy all artifacts.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsArtifactPredicate()));
    }
}
