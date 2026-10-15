package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringLandNotFromHandConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "129")
public class TheLostAndTheDamned extends Card {

    public TheLostAndTheDamned() {
        CreateTokenEffect spawn = new CreateTokenEffect(
                "Spawn", 3, 3, CardColor.RED, List.of(CardSubtype.SPAWN), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new EnteringLandNotFromHandConditionalEffect(spawn));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PlayFromOutsideHandTriggerEffect(List.of(spawn)));
    }
}
