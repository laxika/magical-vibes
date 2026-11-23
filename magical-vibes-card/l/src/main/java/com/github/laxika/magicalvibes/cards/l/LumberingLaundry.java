package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreaturesEffect;
import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "253")
public class LumberingLaundry extends Card {

    public LumberingLaundry() {
        addMorph("{5}");
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(GrantStaticEffectToPlayerEffect.untilEndOfTurn(new LookAtFaceDownCreaturesEffect())),
                "{2}: Until end of turn, you may look at face-down creatures you don't control any time."));
    }
}
