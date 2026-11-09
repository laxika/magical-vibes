package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterTriggeringSpellAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "143")
@CardRegistration(set = "SLD", collectorNumber = "95")
@CardRegistration(set = "GK2", collectorNumber = "11")
public class Dovescape extends Card {

    public Dovescape() {
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new CounterTriggeringSpellAndCreateTokensEffect(
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new TargetSpellManaValue(),
                                "Bird",
                                1,
                                1,
                                CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLUE),
                                List.of(CardSubtype.BIRD),
                                Set.of(Keyword.FLYING),
                                Set.of(),
                                false,
                                false,
                                List.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of())));
    }
}
