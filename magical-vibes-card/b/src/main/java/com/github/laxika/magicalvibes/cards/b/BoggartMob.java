package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "104")
public class BoggartMob extends Card {

    public BoggartMob() {
        // Champion a Goblin.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChampionCreatureEffect(CardSubtype.GOBLIN));

        // Whenever a Goblin you control deals combat damage to a player, you may create a
        // 1/1 black Goblin Rogue creature token.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        new MayEffect(
                                new CreateTokenEffect("Goblin Rogue", 1, 1, CardColor.BLACK,
                                        List.of(CardSubtype.GOBLIN, CardSubtype.ROGUE), Set.of(), Set.of()),
                                "create a 1/1 black Goblin Rogue creature token")));
    }
}
