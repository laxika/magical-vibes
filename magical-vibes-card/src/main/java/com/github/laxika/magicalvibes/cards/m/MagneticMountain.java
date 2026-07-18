package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaPerTappedCreatureToUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "209")
public class MagneticMountain extends Card {

    public MagneticMountain() {
        PermanentPredicate blueCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));

        // Blue creatures don't untap during their controllers' untap steps.
        addEffect(EffectSlot.STATIC, new MatchingPermanentsDoesntUntapEffect(blueCreature));

        // At the beginning of each player's upkeep, that player may choose any number of tapped
        // blue creatures they control and pay {4} for each creature chosen this way. If the player
        // does, untap those creatures.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new PayManaPerTappedCreatureToUntapEffect(4, blueCreature));
    }
}
