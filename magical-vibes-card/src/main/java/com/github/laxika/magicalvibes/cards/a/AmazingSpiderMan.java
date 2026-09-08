package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WebSlingingEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

public class AmazingSpiderMan extends Card {

    public AmazingSpiderMan() {
        addEffect(EffectSlot.STATIC, new WebSlingingEffect(
                "{G}{W}{U}",
                new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new CardNotPredicate(new CardIsColorlessPredicate())
                ))
        ));
    }
}
