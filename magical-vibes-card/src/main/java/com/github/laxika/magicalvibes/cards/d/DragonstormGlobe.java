package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;

@CardRegistration(set = "TDM", collectorNumber = "241")
public class DragonstormGlobe extends Card {

    public DragonstormGlobe() {
        addEffect(EffectSlot.STATIC,
                new ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype.DRAGON, 1));

        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
