package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "244")
public class MoorlandHaunt extends Card {

    public MoorlandHaunt() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {W}{U}, {T}, Exile a creature card from your graveyard: Create a 1/1 white Spirit creature token with flying.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{U}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        CreateTokenEffect.whiteSpirit(1)
                ),
                "{W}{U}, {T}, Exile a creature card from your graveyard: Create a 1/1 white Spirit creature token with flying."
        ));
    }
}
