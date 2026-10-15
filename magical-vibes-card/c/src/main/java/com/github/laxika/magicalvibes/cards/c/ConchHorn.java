package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutControllerCardFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "83")
public class ConchHorn extends Card {

    public ConchHorn() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(2),
                        new PutControllerCardFromHandOnTopOfLibraryEffect()
                ),
                "{1}, {T}, Sacrifice Conch Horn: Draw two cards, then put a card from your hand on top of your library."
        ));
    }
}
