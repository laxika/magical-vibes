package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

public class CacklingCulprit extends Card {

    public CacklingCulprit() {
        var deathTrigger = new GainLifeEffect(1);
        addEffect(EffectSlot.ON_DEATH, deathTrigger);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, deathTrigger);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                "{1}{B}: This creature gains deathtouch until end of turn."
        ));
    }
}
