package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "141")
public class SylvanHierophant extends Card {

    public SylvanHierophant() {
        // When this creature dies, exile it, then return another target creature card from your
        // graveyard to your hand. The target is chosen as the death trigger goes on the stack, at
        // which point the Hierophant is itself in the graveyard — "another" excludes it via the
        // not-self filter. exileSourceFromGraveyard performs the self-exile before the return.
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardNotPredicate(new CardIsSelfPredicate()))))
                .targetGraveyard(true)
                .exileSourceFromGraveyard(true)
                .build());
    }
}
