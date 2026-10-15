package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "ARB", collectorNumber = "112")
public class GrixisSojourners extends Card {

    public GrixisSojourners() {
        // When you cycle this card and when this creature dies, you may exile target card from a graveyard.
        // The target is chosen when each trigger goes on the stack; the "may" is asked on resolution.
        addEffect(EffectSlot.ON_DEATH, new MayEffect(exileTargetedGraveyardCard(),
                "Exile the targeted card from a graveyard?"));
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(exileTargetedGraveyardCard(),
                "Exile the targeted card from a graveyard?"));

        // Cycling {2}{B} ({2}{B}, Discard this card: Draw a card.)
        addCycling("{2}{B}");
    }

    private static ReturnCardFromGraveyardEffect exileTargetedGraveyardCard() {
        return ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.EXILE)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .targetGraveyard(true)
                .build();
    }
}
