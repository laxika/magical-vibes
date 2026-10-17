package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "123")
@CardRegistration(set = "EMN", collectorNumber = "96")
@CardRegistration(set = "SIR", collectorNumber = "123")
public class MidnightScavengers extends Card {

    public MidnightScavengers() {
        setBackFaceCard(new ChitteringHost());

        // When this creature enters, you may return target creature card with mana value 3 or less
        // from your graveyard to your hand. The target is chosen when the ability goes on the stack;
        // the "may" is decided on resolution.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(3))))
                .targetGraveyard(true)
                .build(), "Return the targeted creature card to your hand?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChitteringHost";
    }
}
