package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "2XM", collectorNumber = "204")
public class KaaliaOfTheVast extends Card {

    public KaaliaOfTheVast() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AttackedTargetIsOpponent(),
                PutCardToBattlefieldEffect.tappedAndAttacking(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.ANGEL),
                                        new CardSubtypePredicate(CardSubtype.DEMON),
                                        new CardSubtypePredicate(CardSubtype.DRAGON))))),
                        "Angel, Demon, or Dragon creature")));
    }
}
