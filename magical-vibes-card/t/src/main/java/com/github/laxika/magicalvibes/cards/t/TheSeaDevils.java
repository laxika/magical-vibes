package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "108")
@CardRegistration(set = "WHO", collectorNumber = "713")
public class TheSeaDevils extends Card {

    private static final CreateTokenEffect SALAMANDER_TOKEN = new CreateTokenEffect(
        1, "Alien Salamander", 2, 2, CardColor.GREEN,
            List.of(CardSubtype.ALIEN, CardSubtype.SALAMANDER), Set.of(Keyword.ISLANDWALK), Set.of());

    public TheSeaDevils() {
        addTrigger(Triggers.chapter(1), SALAMANDER_TOKEN);
        addTrigger(Triggers.chapter(2), SALAMANDER_TOKEN);
        // Chapter III: until end of turn, whenever a Salamander (any controller's) deals combat damage
        // to a player, it deals that much damage to target creature that player controls.
        addTrigger(Triggers.chapter(3), new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.creatureDealsCombatDamageToPlayer()
                                .withFilter(new PermanentHasSubtypePredicate(CardSubtype.SALAMANDER)),
                        new DealDamageToTargetCreatureEffect(new EventValue(), false,
                                new PermanentControlledByTriggeringPlayerPredicate()))));
    }
}
