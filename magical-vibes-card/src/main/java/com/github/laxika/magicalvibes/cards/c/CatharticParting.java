package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "171")
public class CatharticParting extends Card {

    public CatharticParting() {
        PermanentAllOfPredicate artifactOrEnchantmentOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        target(new PermanentPredicateTargetFilter(
                artifactOrEnchantmentOpponentControls,
                "Target must be an artifact or enchantment an opponent controls"
        )).addEffect(EffectSlot.SPELL, new ShuffleTargetPermanentIntoLibraryEffect());
        addEffect(EffectSlot.SPELL,
                new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(null, 4));
    }
}
