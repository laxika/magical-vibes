package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "13")
@CardRegistration(set = "OPCA", collectorNumber = "30")
public class Goldmeadow extends Card {

    public Goldmeadow() {
        CreateTokenEffect goats = new CreateTokenEffect(
                3, "Goat", 0, 1, CardColor.WHITE, List.of(CardSubtype.GOAT), Set.of(), Set.of());
        CreateTokenForTriggeringPlayerEffect landfall = new CreateTokenForTriggeringPlayerEffect(goats, false, true);
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), landfall);
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS), landfall);

        addEffect(EffectSlot.CHAOS_TRIGGERED, new CreateTokenEffect(
                "Goat", 0, 1, CardColor.WHITE, List.of(CardSubtype.GOAT), Set.of(), Set.of()));
    }
}
