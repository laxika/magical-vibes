package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "150")
@CardRegistration(set = "IMA", collectorNumber = "149")
@CardRegistration(set = "SCD", collectorNumber = "164")
public class ThundermawHellkite extends Card {

    public ThundermawHellkite() {
        final PermanentPredicate opponentFliers = new PermanentAllOfPredicate(List.of(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDamageEffect.toEachCreature(1, opponentFliers),
                new TapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), opponentFliers)))));
    }
}
