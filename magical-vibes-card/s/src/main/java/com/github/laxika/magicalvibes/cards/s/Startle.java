package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "78")
@CardRegistration(set = "DBL", collectorNumber = "78")
public class Startle extends Card {

    public Startle() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-2, 0));
        addSpellEffect(CreateTokenEffect.blackZombieWithDecayed(1));
        addSpellEffect(new DrawCardEffect(1));
    }
}
