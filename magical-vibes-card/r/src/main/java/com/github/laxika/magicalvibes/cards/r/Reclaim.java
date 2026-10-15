package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "ORI", collectorNumber = "195")
@CardRegistration(set = "M12", collectorNumber = "191")
@CardRegistration(set = "9ED", collectorNumber = "264")
@CardRegistration(set = "7ED", collectorNumber = "263")
@CardRegistration(set = "EXO", collectorNumber = "120")
public class Reclaim extends Card {

    public Reclaim() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                .targetGraveyard(true)
                .build());
    }
}
