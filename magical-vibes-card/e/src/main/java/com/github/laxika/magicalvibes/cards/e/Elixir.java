package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "256")
public class Elixir extends Card {

    public Elixir() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        CardNotPredicate nonland = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ExileSelfCost(),
                        new ShuffleGraveyardIntoLibraryEffect(false, nonland),
                        new GainLifeEffect(new EventValue())
                ),
                "{5}, {T}, Exile Elixir: Shuffle all nonland cards from your graveyard into your library. "
                        + "You gain life equal to the number of cards shuffled into your library this way."
        ));
    }
}
