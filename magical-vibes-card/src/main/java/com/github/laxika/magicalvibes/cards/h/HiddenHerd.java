package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "262")
public class HiddenHerd extends Card {

    public HiddenHerd() {
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND, new TriggeringCardConditionalEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.LAND),
                        new CardNotPredicate(new CardSupertypePredicate(CardSupertype.BASIC))
                )),
                new ConditionalEffect(new SourceIsEnchantment(),
                        new BecomeCreatureEffect(3, 3, CardSubtype.BEAST))
        ));
    }
}
