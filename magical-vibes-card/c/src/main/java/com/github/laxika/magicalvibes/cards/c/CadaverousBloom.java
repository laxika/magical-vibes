package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "258")
public class CadaverousBloom extends Card {

    public CadaverousBloom() {
        // Exile a card from your hand: Add {B}{B} or {G}{G}. Modeled as two mana abilities, one per
        // color option (same idiom as Adarkar Wastes / Land Cap).
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileCardFromHandCost(), new AwardManaEffect(ManaColor.BLACK, 2)),
                "Exile a card from your hand: Add {B}{B}."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileCardFromHandCost(), new AwardManaEffect(ManaColor.GREEN, 2)),
                "Exile a card from your hand: Add {G}{G}."
        ));
    }
}
