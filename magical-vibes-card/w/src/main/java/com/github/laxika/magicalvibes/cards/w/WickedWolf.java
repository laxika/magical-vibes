package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "181")
@CardRegistration(set = "ELD", collectorNumber = "374")
public class WickedWolf extends Card {

    public WickedWolf() {
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new SourceFightsTargetCreatureEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                                "Sacrifice a Food"),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source()),
                        new TapPermanentsEffect(Subjects.source())),
                "Sacrifice a Food: Put a +1/+1 counter on this creature. It gains indestructible until end of turn. Tap it."
        ));
    }
}
