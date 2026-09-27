package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachOpponentAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "32")
@CardRegistration(set = "SOC", collectorNumber = "80")
public class FurygaleFlocking extends Card {

    public FurygaleFlocking() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new CardsInGraveyard(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                CountScope.CONTROLLER)));

        addEffect(EffectSlot.SPELL, new CreateTokensForEachOpponentAttackingEffect(
                new CreateTokenEffect(
                        CardType.CREATURE,
                        2,
                        "Elemental",
                        3,
                        3,
                        CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.RED),
                        List.of(CardSubtype.ELEMENTAL),
                        Set.of(Keyword.FLYING),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of(Keyword.HASTE))));
    }
}
