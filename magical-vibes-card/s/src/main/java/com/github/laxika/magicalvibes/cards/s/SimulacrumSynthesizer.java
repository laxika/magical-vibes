package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BIG", collectorNumber = "6")
@CardRegistration(set = "MAR", collectorNumber = "61")
public class SimulacrumSynthesizer extends Card {

    public SimulacrumSynthesizer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));

        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        CreateTokenEffect construct = new CreateTokenEffect(
                1, "Construct", 0, 0,
                null, List.of(CardSubtype.CONSTRUCT),
                Set.of(), Set.of(CardType.ARTIFACT),
                List.of(StaticAbility.of(new BoostSelfEffect(artifactsYouControl, artifactsYouControl)))
        );
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.ARTIFACT, new PermanentMinManaValuePredicate(3))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                construct);
    }
}
