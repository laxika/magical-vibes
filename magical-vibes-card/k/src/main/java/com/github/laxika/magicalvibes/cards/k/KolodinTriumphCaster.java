package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "210")
public class KolodinTriumphCaster extends Card {

    public KolodinTriumphCaster() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.HASTE,
                GrantScope.OWN_PERMANENTS,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MOUNT, CardSubtype.VEHICLE))));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.MOUNT))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new BecomeSaddledUntilEndOfTurnEffect(GrantScope.TRIGGERING_PERMANENT));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ARTIFACT,
                                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new AnimatePermanentsEffect(
                        null, null, List.of(), Set.of(), null, Set.of(CardType.CREATURE),
                        GrantScope.TRIGGERING_PERMANENT, EffectDuration.UNTIL_END_OF_TURN, null));
    }
}
