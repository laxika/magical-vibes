package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.IgnoreOpponentHexproofUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "249")
public class DetectionTower extends Card {

    public DetectionTower() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new IgnoreOpponentHexproofUntilEndOfTurnEffect()),
                "{1}, {T}: Until end of turn, your opponents and creatures your opponents control "
                        + "with hexproof can be the targets of spells and abilities you control as "
                        + "though they didn't have hexproof."
        ));
    }
}
