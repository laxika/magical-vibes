package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "28")
public class SkyfirePhoenix extends Card {

    public SkyfirePhoenix() {
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)),
                new StackEntryCastFromZonePredicate(Zone.COMMAND)
        ));
    }
}
