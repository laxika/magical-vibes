package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "282")
public class SilvosRogueElemental extends Card {

    public SilvosRogueElemental() {
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new RegenerateEffect()), "{G}: Regenerate Silvos, Rogue Elemental."));
    }
}
