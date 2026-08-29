package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "137")
public class MorbiusTheLivingVampire extends Card {

    public MorbiusTheLivingVampire() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{U}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3))
                ),
                "{U}{B}, Exile this card from your graveyard: Look at the top three cards of your library. "
                        + "Put one of them into your hand and the rest on the bottom of your library in any order."
        ));
    }
}
