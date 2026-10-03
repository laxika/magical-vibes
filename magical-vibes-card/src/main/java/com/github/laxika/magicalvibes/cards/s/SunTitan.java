package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "35")
@CardRegistration(set = "M12", collectorNumber = "39")
@CardRegistration(set = "DDL", collectorNumber = "1")
@CardRegistration(set = "SLD", collectorNumber = "1550")
@CardRegistration(set = "SLC", collectorNumber = "72")
@CardRegistration(set = "HA7", collectorNumber = "1")
@CardRegistration(set = "SOC", collectorNumber = "178")
@CardRegistration(set = "C14", collectorNumber = "91")
@CardRegistration(set = "C15", collectorNumber = "82")
@CardRegistration(set = "C20", collectorNumber = "101")
@CardRegistration(set = "FIC", collectorNumber = "254")
@CardRegistration(set = "C21", collectorNumber = "106")
@CardRegistration(set = "NCC", collectorNumber = "210")
@CardRegistration(set = "TDC", collectorNumber = "133")
@CardRegistration(set = "MKC", collectorNumber = "87")
@CardRegistration(set = "AFC", collectorNumber = "73")
@CardRegistration(set = "OTC", collectorNumber = "87")
@CardRegistration(set = "BLC", collectorNumber = "157")
@CardRegistration(set = "C19", collectorNumber = "76")
@CardRegistration(set = "WOC", collectorNumber = "77")
@CardRegistration(set = "KHC", collectorNumber = "34")
public class SunTitan extends Card {

    public SunTitan() {
        // Whenever Sun Titan enters the battlefield or attacks,
        // you may return target permanent card with mana value 3 or less
        // from your graveyard to the battlefield.
        MayEffect returnEffect = new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardMaxManaValuePredicate(3)
                        )))
                        .build(),
                "You may return target permanent card with mana value 3 or less from your graveyard to the battlefield."
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, returnEffect);
        addEffect(EffectSlot.ON_ATTACK, returnEffect);
    }
}
