package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "229")
@CardRegistration(set = "DRB", collectorNumber = "15")
public class TwoHeadedDragon extends Card {

    public TwoHeadedDragon() {
        // Flying and Menace come from the Scryfall keyword data.
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new BoostSelfEffect(2, 0)), "{1}{R}: This creature gets +2/+0 until end of turn."));
    }
}
