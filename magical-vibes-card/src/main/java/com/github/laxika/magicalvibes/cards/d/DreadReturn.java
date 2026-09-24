package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "104")
@CardRegistration(set = "PD3", collectorNumber = "23")
@CardRegistration(set = "DDQ", collectorNumber = "55")
@CardRegistration(set = "2XM", collectorNumber = "89")
@CardRegistration(set = "TSR", collectorNumber = "111")
@CardRegistration(set = "DMR", collectorNumber = "80")
@CardRegistration(set = "CMM", collectorNumber = "153")
@CardRegistration(set = "CMM", collectorNumber = "637")
public class DreadReturn extends Card {

    public DreadReturn() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());

        addCastingOption(new FlashbackCast(List.of(
                new SacrificePermanentsCost(3, new PermanentIsCreaturePredicate())
        )));
    }
}
