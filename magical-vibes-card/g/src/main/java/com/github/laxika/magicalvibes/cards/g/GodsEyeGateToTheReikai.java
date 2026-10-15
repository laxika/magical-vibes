package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "164")
public class GodsEyeGateToTheReikai extends Card {

    public GodsEyeGateToTheReikai() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addEffect(EffectSlot.ON_DEATH,
                new CreateTokenEffect("Spirit", 1, 1, null,
                        List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));
    }
}
