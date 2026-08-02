package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "CHK", collectorNumber = "112")
public class GibberingKami extends Card {

    public GibberingKami() {
        // Flying is auto-loaded from Scryfall keywords.
        // Soulshift 3: "When this creature dies, you may return target Spirit card with mana value 3
        // or less from your graveyard to your hand." The graveyard target is chosen as the trigger
        // goes on the stack (processNextDeathTriggerTarget); declining the choice is the "you may".
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardMaxManaValuePredicate(3))))
                .targetGraveyard(true)
                .build());
    }
}
