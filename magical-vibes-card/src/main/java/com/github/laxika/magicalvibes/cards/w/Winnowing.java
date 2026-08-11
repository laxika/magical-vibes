package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureForEachPlayerThenSacrificeNonsharingCreaturesEffect;

@CardRegistration(set = "ECL", collectorNumber = "43")
public class Winnowing extends Card {

    public Winnowing() {
        addEffect(EffectSlot.SPELL, new ChooseCreatureForEachPlayerThenSacrificeNonsharingCreaturesEffect());
    }
}
