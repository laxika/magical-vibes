package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayExileOneAndPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "221")
public class DjeruAndHazoret extends Card {

    public DjeruAndHazoret() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CardsInHandAtMost(1),
                new StaticBoostEffect(0, 0, Set.of(Keyword.VIGILANCE, Keyword.HASTE), GrantScope.SELF)));

        addEffect(EffectSlot.ON_ATTACK, new LookAtTopCardsMayExileOneAndPlayThisTurnEffect(
                6,
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSupertypePredicate(CardSupertype.LEGENDARY))),
                true));
    }
}
