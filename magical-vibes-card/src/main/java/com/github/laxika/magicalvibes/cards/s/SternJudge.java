package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifePerPermanentControlledEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "16")
public class SternJudge extends Card {

    public SternJudge() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EachPlayerLosesLifePerPermanentControlledEffect(
                        1,
                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP))),
                "{T}: Each player loses 1 life for each Swamp they control."
        ));
    }
}
