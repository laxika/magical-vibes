package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleThisTurnEffect;

@CardRegistration(set = "ORI", collectorNumber = "183")
public class JoragaInvocation extends Card {

    public JoragaInvocation() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(3, 3));
        addEffect(EffectSlot.SPELL, new MustBeBlockedIfAbleThisTurnEffect(GrantScope.OWN_CREATURES));
    }
}
