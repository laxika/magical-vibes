package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CreatureWithDifferentNameDiedThisTurn;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "AFR", collectorNumber = "100")
public class EbondeathDracolich extends Card {

    public EbondeathDracolich() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addCastingOption(new GraveyardCast(
                new CreatureWithDifferentNameDiedThisTurn("Ebondeath, Dracolich")));
    }
}
