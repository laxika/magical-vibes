package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesCreatureEffect;

@CardRegistration(set = "YSOS", collectorNumber = "20")
public class GraveStudies extends Card {

    public GraveStudies() {
        // Conjure a card named Teacher's Pest onto the battlefield, then each player sacrifices a
        // creature.
        addEffect(EffectSlot.SPELL, new ConjureCardToBattlefieldEffect("Teacher's Pest"));
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesCreatureEffect());
    }
}
