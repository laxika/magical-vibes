package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "113")
public class ElephantResurgence extends Card {

    public ElephantResurgence() {
        CardsInGraveyard creatureCards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        CreateTokenEffect elephant = new CreateTokenEffect(
                1, "Elephant", 0, 0, CardColor.GREEN, List.of(CardSubtype.ELEPHANT),
                Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(creatureCards, creatureCards)));
        addEffect(EffectSlot.SPELL, new EachPlayerCreatesTokenEffect(elephant));
    }
}
