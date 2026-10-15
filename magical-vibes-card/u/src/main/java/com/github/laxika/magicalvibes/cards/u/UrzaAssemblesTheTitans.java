package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantExtraLoyaltyActivationToPlaneswalkersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "37")
public class UrzaAssemblesTheTitans extends Card {

    public UrzaAssemblesTheTitans() {
        CardPredicate planeswalker = new CardTypePredicate(CardType.PLANESWALKER);
        CardPredicate planeswalkerWithManaValueAtMostSix = new CardAllOfPredicate(List.of(
                planeswalker,
                new CardMaxManaValuePredicate(6)
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_I, new ScryEffect(4));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MayEffect(
                new RevealTopCardMatchingToHandEffect(planeswalker),
                "Reveal the top card of your library?"
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new MayEffect(
                new PutCardToBattlefieldEffect(planeswalkerWithManaValueAtMostSix, "planeswalker"),
                "Put a planeswalker card with mana value 6 or less from your hand onto the battlefield?"
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new GrantExtraLoyaltyActivationToPlaneswalkersEffect());
    }
}
