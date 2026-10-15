package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.CreatureDeathsThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceDownCommandZoneCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "332")
public class DarkWingsBringYourDownfall extends Card {

    public DarkWingsBringYourDownfall() {
        addEffect(EffectSlot.COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK,
                new CreateTokenEffect(1, "Demon", 5, 5, CardColor.BLACK,
                        List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), true, false));

        addEffect(EffectSlot.COMMAND_ZONE_EACH_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new CreatureDeathsThisTurnAtLeast(2, CountScope.CONTROLLER),
                        new TurnFaceDownCommandZoneCardEffect()));
    }
}
