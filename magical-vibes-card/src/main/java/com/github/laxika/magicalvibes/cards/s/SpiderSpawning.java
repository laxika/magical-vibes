package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "203")
public class SpiderSpawning extends Card {

    public SpiderSpawning() {
        // Create a 1/2 green Spider creature token with reach for each creature card in your graveyard.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                "Spider", 1, 2, CardColor.GREEN, List.of(CardSubtype.SPIDER),
                Set.of(Keyword.REACH), Set.of()
        ));
        addCastingOption(new FlashbackCast("{6}{B}"));
    }
}
