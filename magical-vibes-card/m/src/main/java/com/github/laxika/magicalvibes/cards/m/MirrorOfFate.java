package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MirrorOfFateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "215")
public class MirrorOfFate extends Card {

    public MirrorOfFate() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new MirrorOfFateEffect()
                ),
                "{T}, Sacrifice Mirror of Fate: Choose up to seven face-up exiled cards you own. Exile all the cards from your library, then put the chosen cards on top of your library."
        ));
    }
}
