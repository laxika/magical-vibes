package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "183")
public class HedgeShredder extends Card {

    public HedgeShredder() {
        addEffect(EffectSlot.ON_ATTACK,
                new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));
        addEffect(EffectSlot.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
                new ReturnTriggeringLandFromGraveyardToBattlefieldEffect(null, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
