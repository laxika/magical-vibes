package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCardPutIntoHandFromLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "21")
public class KithkinBrinefarer extends Card {

    public KithkinBrinefarer() {
        addEffect(EffectSlot.ON_CONTROLLER_CARD_PUT_INTO_HAND_FROM_LIBRARY,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.KITHKIN),
                        new ConjureDuplicateOfCardPutIntoHandFromLibraryEffect()));
        addEffect(EffectSlot.ON_ATTACK,
                new PerpetuallyBoostMatchingHandCardsEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.KITHKIN),
                                new CardTypePredicate(CardType.CREATURE))), 1, 1));
    }
}
