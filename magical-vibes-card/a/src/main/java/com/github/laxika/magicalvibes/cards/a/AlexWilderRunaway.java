package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentWasCastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "677")
public class AlexWilderRunaway extends Card {

    public AlexWilderRunaway() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.NONTOKEN,
                                        new PermanentWasCastPredicate(),
                                        new PermanentNotPredicate(new PermanentCastFromZonePredicate(Zone.HAND)))))),
                        ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(
                        BoostEffect.of(Subjects.triggering(), 2, 0),
                        new GrantKeywordEffect(Keyword.HASTE, Subjects.triggering())));

        addCastingOption(new GraveyardCast(null, "{2}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 3)),
                null, false, false, true));
    }
}
