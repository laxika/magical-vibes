package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToAllNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "GPT", collectorNumber = "29")
public class LeylineOfSingularity extends Card {

    public LeylineOfSingularity() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Singularity on the battlefield?"
        ));
        addEffect(EffectSlot.STATIC,
                new GrantSupertypeToAllNonlandPermanentsEffect(CardSupertype.LEGENDARY));
    }
}
