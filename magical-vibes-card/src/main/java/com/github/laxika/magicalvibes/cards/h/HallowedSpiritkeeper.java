package com.github.laxika.magicalvibes.cards.h;

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
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "8")
public class HallowedSpiritkeeper extends Card {

    public HallowedSpiritkeeper() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                "Spirit", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()));
    }
}
