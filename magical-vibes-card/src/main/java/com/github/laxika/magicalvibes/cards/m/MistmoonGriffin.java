package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardOwner;
import com.github.laxika.magicalvibes.model.effect.ReturnTopCreatureCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WTH", collectorNumber = "21")
public class MistmoonGriffin extends Card {

    public MistmoonGriffin() {
        // When this creature dies, exile it, then return the top creature card of your graveyard to
        // the battlefield. Both halves are one triggered ability, so the self-exile has to happen
        // before the reanimation is looked up — otherwise the Griffin itself would be the top
        // creature card.
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new ExileSourceCardFromGraveyardEffect(),
                new ReturnTopCreatureCardFromGraveyardToBattlefieldEffect(GraveyardOwner.CONTROLLER, false)));
    }
}
