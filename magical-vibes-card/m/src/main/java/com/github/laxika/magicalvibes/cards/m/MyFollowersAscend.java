package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "343")
public class MyFollowersAscend extends Card {

    public MyFollowersAscend() {
        PermanentPredicate creature = new PermanentIsCreaturePredicate();
        ControlsPermanent controlsCreature = new ControlsPermanent(creature);

        addSpellEffect(new ConditionalEffect(controlsCreature,
                        SequenceEffect.of(
                                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(), creature),
                                        "a creature you control"),
                                new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE),
                                        CounterType.PLUS_ONE_PLUS_ONE, 5),
                                new GrantKeywordEffect(Keyword.FLYING, Subjects.thisWay(StepKind.CHOOSE),
                                        Duration.UNTIL_END_OF_TURN),
                                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.thisWay(StepKind.CHOOSE),
                                        Duration.UNTIL_END_OF_TURN))));
        addSpellEffect(new ConditionalEffect(new NotCondition(controlsCreature),
                        new CreateTokenEffect(
                                "Scarecrow", 4, 4, null,
                                List.of(CardSubtype.SCARECROW), Set.of(Keyword.VIGILANCE),
                                Set.of(CardType.ARTIFACT))));
    }
}
