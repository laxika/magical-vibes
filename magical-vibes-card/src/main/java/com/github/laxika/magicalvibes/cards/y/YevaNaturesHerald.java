package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "197")
public class YevaNaturesHerald extends Card {

    public YevaNaturesHerald() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardColorPredicate(CardColor.GREEN)))));
    }
}
