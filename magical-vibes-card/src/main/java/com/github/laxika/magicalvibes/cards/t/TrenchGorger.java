package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "CMD", collectorNumber = "65")
public class TrenchGorger extends Card {

    public TrenchGorger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        SequenceEffect.of(
                                new SearchLibraryForCardsToExileWithSourceEffect(
                                        new CardTypePredicate(CardType.LAND)),
                                new SetBasePowerToughnessToAmountEffect(
                                        new CardsExiledWithSource(),
                                        new CardsExiledWithSource(),
                                        GrantScope.SELF)),
                        "Search your library for any number of land cards?"));
    }
}
