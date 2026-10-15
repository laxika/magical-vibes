package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2200")
public class HauntedOne extends Card {

    public HauntedOne() {
        PermanentPredicate commanderCreatureYouOwn = new PermanentAllOfPredicate(List.of(
                new PermanentIsCommanderPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentOwnedBySourceControllerPredicate()));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(commanderCreatureYouOwn,
                        new BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect(
                                2, 0, Set.of(Keyword.UNDYING))));
    }
}
