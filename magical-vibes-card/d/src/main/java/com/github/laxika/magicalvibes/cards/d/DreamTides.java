package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaPerTappedCreatureToUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VIS", collectorNumber = "31")
public class DreamTides extends Card {

    public DreamTides() {
        PermanentPredicate nongreenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.GREEN)))));

        // Creatures don't untap during their controllers' untap steps.
        addEffect(EffectSlot.STATIC, new MatchingPermanentsDoesntUntapEffect(new PermanentIsCreaturePredicate()));

        // At the beginning of each player's upkeep, that player may choose any number of tapped
        // nongreen creatures they control and pay {2} for each creature chosen this way. If the
        // player does, untap those creatures.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new PayManaPerTappedCreatureToUntapEffect(2, nongreenCreature));
    }
}
