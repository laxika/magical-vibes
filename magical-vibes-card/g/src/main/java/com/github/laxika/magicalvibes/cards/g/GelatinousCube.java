package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "105")
public class GelatinousCube extends Card {

    public GelatinousCube() {
        PermanentPredicate targetCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.OOZE)),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                targetCreature,
                "Target must be a non-Ooze creature an opponent controls"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentUntilSourceLeavesEffect(false, targetCreature));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{B}",
                List.of(new PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect(
                        new CardTypePredicate(CardType.CREATURE), true)),
                "{X}{B}: Put target creature card with mana value X exiled with Gelatinous Cube into its owner's graveyard."
        ));
    }
}
