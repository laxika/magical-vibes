package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "138")
public class DelinaWildMage extends Card {

    public DelinaWildMage() {
        CreateTokenCopyOfTargetPermanentEffect tokenCopy =
                new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(), null, null, Map.of(), false, false, false, true,
                        false, false, null, Set.of(), false,
                        List.of(new TriggeredAbility(Triggers.endOfCombat(), new ExileSelfEffect())),
                        List.of(), false, true, new Fixed(1), false, Set.of(), true);
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.attacks(),
                        RollD20Effect.withRepeatOnHighBranch(tokenCopy, tokenCopy, 14));
    }
}
