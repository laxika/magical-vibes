package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "104")
public class BoggartMob extends Card {

    public BoggartMob() {
        // Champion a Goblin.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChampionCreatureEffect(CardSubtype.GOBLIN));

        // Whenever a Goblin you control deals combat damage to a player, you may create a
        // 1/1 black Goblin Rogue creature token.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)),
                new MayEffect(
                        new CreateTokenEffect("Goblin Rogue", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.GOBLIN, CardSubtype.ROGUE), Set.of(), Set.of()),
                        "create a 1/1 black Goblin Rogue creature token"));
    }
}
