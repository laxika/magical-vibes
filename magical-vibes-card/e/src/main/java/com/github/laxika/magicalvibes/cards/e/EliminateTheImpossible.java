package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.UnsuspectAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

@CardRegistration(set = "MKM", collectorNumber = "54")
public class EliminateTheImpossible extends Card {

    public EliminateTheImpossible() {
        PermanentPredicate opponentPermanent =
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, 0, opponentPermanent));
        addEffect(EffectSlot.SPELL, new UnsuspectAllCreaturesEffect(opponentPermanent));
    }
}
