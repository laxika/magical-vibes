package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForNonlandCardAndRevealEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "66")
public class GrimReminder extends Card {

    public GrimReminder() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForNonlandCardAndRevealEffect(6));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{B}{B}: Return Grim Reminder from your graveyard to your hand. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
