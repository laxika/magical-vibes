package com.github.laxika.magicalvibes.cards.t;

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
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "149")
public class TombstoneStairwell extends Card {

    public TombstoneStairwell() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}{B}"));

        // The Tombspawn count is CONTROLLER-scoped so it is re-evaluated per creating player,
        // reading each player's own graveyard.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new EachPlayerCreatesTokenEffect(
                new CreateTokenEffect(
                        new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                        "Tombspawn", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE),
                        Set.of(Keyword.HASTE), Set.of()),
                true));

        addEffect(EffectSlot.END_STEP_TRIGGERED, new DestroyTokensCreatedWithSourceEffect(true));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DestroyTokensCreatedWithSourceEffect(true));
    }
}
