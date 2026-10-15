package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "145")
@CardRegistration(set = "2X2", collectorNumber = "123")
public class SeasonedPyromancer extends Card {

    public SeasonedPyromancer() {
        CreateTokenEffect elemental = new CreateTokenEffect(
                "Elemental", 1, 1, CardColor.RED, List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DiscardCardThenEffect(
                        null, elemental, "a card",
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                new DiscardCardThenEffect(
                        null, elemental, "a card",
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                new DrawCardEffect(2)
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect(
                                2, "Elemental", 1, 1, CardColor.RED,
                                List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of())
                ),
                "{3}{R}{R}, Exile this card from your graveyard: Create two 1/1 red Elemental creature tokens."
        ));
    }
}
