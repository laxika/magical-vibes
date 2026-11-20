package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "142")
public class RundveltHordemaster extends Card {

    public RundveltHordemaster() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));

        ExileTopCardMayPlayThisTurnEffect deathEffect =
                new ExileTopCardMayPlayThisTurnEffect(1, false)
                        .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)
                        .mayOnlyCast(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.GOBLIN))));
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                deathEffect);
    }
}
