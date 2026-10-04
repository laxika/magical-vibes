package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FDC", collectorNumber = "266")
public class InfernalIdol extends Card {

    public InfernalIdol() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(2),
                        new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER)
                ),
                "{1}{B}{B}, {T}, Sacrifice this artifact: You draw two cards and lose 2 life."
        ));
    }
}
