package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "119")
public class TymaretChosenFromDeath extends Card {

    public TymaretChosenFromDeath() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new Fixed(2), new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new ExileCardsFromGraveyardEffect(
                        2, new CardTypePredicate(CardType.CREATURE), 0, 1, false, true)),
                "{1}{B}: Exile up to two target cards from graveyards. You gain 1 life for each creature card exiled this way.",
                List.of(),
                0,
                2
        ));
    }
}
