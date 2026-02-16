package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

public class DoomedNecromancer extends Card {

    public DoomedNecromancer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new SacrificeSelfCost(), new ReturnCreatureFromGraveyardToBattlefieldEffect()),
                false,
                "{B}, {T}, Sacrifice Doomed Necromancer: Return target creature card from your graveyard to the battlefield."
        ));
    }
}
