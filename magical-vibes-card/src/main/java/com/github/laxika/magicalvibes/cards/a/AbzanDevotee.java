package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "68")
public class AbzanDevotee extends Card {

    public AbzanDevotee() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK, ManaColor.GREEN))),
                "{1}: Add {W}, {B}, or {G}. Activate only once each turn.",
                1
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{2}{B}: Return this card from your graveyard to your hand."
        ));
    }
}
