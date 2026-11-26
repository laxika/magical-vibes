package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentProtectedByOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "243")
public class JoyfulStormsculptor extends Card {

    public JoyfulStormsculptor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect(2, "Elemental", 1, 1, null,
                        Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)));

        addSimultaneousTrigger(Triggers.youCastSpell().withFilter(new CardKeywordPredicate(Keyword.CONVOKE)),
                DealDamageEffect.to(Subjects.eachOpponent(), 1),
                DealDamageEffect.toEach(
                        Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentAllOfPredicate(List.of(
                                new PermanentIsBattlePredicate(),
                                new PermanentProtectedByOpponentOfSourceControllerPredicate()))), 1));
    }
}
