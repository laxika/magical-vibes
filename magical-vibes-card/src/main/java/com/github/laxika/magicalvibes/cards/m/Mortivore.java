package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "161")
public class Mortivore extends Card {

    public Mortivore() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                false,
                "{B}: Regenerate Mortivore."
        ));
    }
}
