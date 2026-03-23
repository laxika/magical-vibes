package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "11")
public class EncampmentKeeper extends Card {

    public EncampmentKeeper() {
        // {7}{W}, {T}, Sacrifice Encampment Keeper: Creatures you control get +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}{W}",
                List.of(new SacrificeSelfCost(), new BoostAllOwnCreaturesEffect(2, 2)),
                "{7}{W}, {T}, Sacrifice Encampment Keeper: Creatures you control get +2/+2 until end of turn."
        ));
    }
}
