package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;

@CardRegistration(set = "RTR", collectorNumber = "248")
@CardRegistration(set = "GRN", collectorNumber = "258")
@CardRegistration(set = "RAV", collectorNumber = "284")
@CardRegistration(set = "ECL", collectorNumber = "268")
@CardRegistration(set = "ECL", collectorNumber = "351")
@CardRegistration(set = "EXP", collectorNumber = "10")
@CardRegistration(set = "SLD", collectorNumber = "127")
@CardRegistration(set = "RVR", collectorNumber = "290")
@CardRegistration(set = "RVR", collectorNumber = "300")
public class TempleGarden extends Card {

    public TempleGarden() {
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(2));

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
