package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardOnDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "245")
public class SaffiEriksdotter extends Card {

    public SaffiEriksdotter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), ReturnTargetCardOnDeathThisTurnEffect.fromControllerGraveyard()),
                "Sacrifice Saffi Eriksdotter: When target creature is put into your graveyard this turn, return that card to the battlefield.",
                TargetFilters.creature()
        ));
    }
}
