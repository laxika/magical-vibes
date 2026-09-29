package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "33")
public class SpawningKraken extends Card {

    public SpawningKraken() {
        PermanentAnyOfPredicate seaMonster = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.KRAKEN),
                new PermanentHasSubtypePredicate(CardSubtype.LEVIATHAN),
                new PermanentHasSubtypePredicate(CardSubtype.OCTOPUS),
                new PermanentHasSubtypePredicate(CardSubtype.SERPENT)
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        seaMonster,
                        new CreateTokenEffect("Kraken", 9, 9, CardColor.BLUE,
                                List.of(CardSubtype.KRAKEN), Set.of(), Set.of())));
    }
}
