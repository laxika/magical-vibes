package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "21")
@CardRegistration(set = "TPR", collectorNumber = "15")
public class GerrardsBattleCry extends Card {

    public GerrardsBattleCry() {
        // {2}{W}: Creatures you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{2}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
