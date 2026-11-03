package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(seaMonster),
                new CreateTokenEffect("Kraken", 9, 9, CardColor.BLUE, List.of(CardSubtype.KRAKEN), Set.of(), Set.of()));
    }
}
