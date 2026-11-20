package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "107")
public class EmbodimentOfFury extends Card {

    public EmbodimentOfFury() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.TRAMPLE,
                Subjects.creaturesYouControl(),
                new PermanentIsLandPredicate()));

        target(TargetFilters.landYouControl()).addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new MayEffect(
                        new AnimatePermanentsEffect(
                                3, 3,
                                List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.HASTE),
                                null, Set.of(),
                                Subjects.target(),
                                Duration.UNTIL_END_OF_TURN),
                        "Have target land you control become a 3/3 Elemental creature with haste until end of turn?"));
    }
}
