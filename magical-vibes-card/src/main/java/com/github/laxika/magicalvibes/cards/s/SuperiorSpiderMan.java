package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;

import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "155")
@CardRegistration(set = "SPM", collectorNumber = "275")
public class SuperiorSpiderMan extends Card {

    public SuperiorSpiderMan() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CopyCreatureCardInGraveyardOnEnterEffect(
                        "Superior Spider-Man", 4, 4,
                        Set.of(CardSubtype.SPIDER, CardSubtype.HUMAN, CardSubtype.HERO)));
    }
}
