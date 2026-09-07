package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreaturesUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "253")
public class LumberingLaundry extends Card {

    public LumberingLaundry() {
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new LookAtFaceDownCreaturesUntilEndOfTurnEffect()),
                "{2}: Until end of turn, you may look at face-down creatures you don't control any time."));
    }
}
