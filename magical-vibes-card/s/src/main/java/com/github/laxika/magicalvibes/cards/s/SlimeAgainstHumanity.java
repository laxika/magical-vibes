package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "177")
public class SlimeAgainstHumanity extends Card {

    public SlimeAgainstHumanity() {
        CardPredicate oozeOrSlimeAgainstHumanity = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.OOZE),
                new CardNamedPredicate("Slime Against Humanity")
        ));
        DynamicAmount counterAmount = new Sum(
                new Fixed(2),
                new CardsInGraveyard(oozeOrSlimeAgainstHumanity, CountScope.CONTROLLER),
                new CardsInExile(oozeOrSlimeAgainstHumanity, CountScope.CONTROLLER));

        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Ooze", 0, 0, CardColor.GREEN, List.of(CardSubtype.OOZE),
                Set.of(Keyword.TRAMPLE), Set.of()));
        addEffect(EffectSlot.SPELL, new PutCountersOnCreatedPermanentsEffect(
                CounterType.PLUS_ONE_PLUS_ONE, counterAmount));
    }
}
