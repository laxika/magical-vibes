package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "141")
public class PromisedKannushi extends Card {

    public PromisedKannushi() {
        // Soulshift 7: "When this creature dies, you may return target Spirit card with mana value 7
        // or less from your graveyard to your hand." The graveyard target is chosen as the trigger
        // goes on the stack; declining the choice is the "you may".
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardMaxManaValuePredicate(7))))
                .targetGraveyard(true)
                .build());
    }
}
