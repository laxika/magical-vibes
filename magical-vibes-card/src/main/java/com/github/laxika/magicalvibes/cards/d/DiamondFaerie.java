package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "128")
public class DiamondFaerie extends Card {

    public DiamondFaerie() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{S}",
                List.of(new BoostAllOwnCreaturesEffect(
                        1, 1, new PermanentHasSupertypePredicate(CardSupertype.SNOW))),
                "{1}{S}: Snow creatures you control get +1/+1 until end of turn."
        ));
    }
}
