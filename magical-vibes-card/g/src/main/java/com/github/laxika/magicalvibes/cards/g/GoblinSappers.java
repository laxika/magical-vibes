package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "189")
public class GoblinSappers extends Card {

    public GoblinSappers() {
        // {R}{R}, {T}: Target creature you control can't be blocked this turn. Destroy it and this
        // creature at end of combat.
        addActivatedAbility(new ActivatedAbility(
                true, "{R}{R}",
                List.of(
                        new MakeCreatureUnblockableEffect(),
                        new DestroyTargetPermanentAtEndOfCombatEffect(),
                        new DestroySelfAtEndOfCombatEffect()
                ),
                "{R}{R}, {T}: Target creature you control can't be blocked this turn. Destroy it "
                        + "and Goblin Sappers at end of combat.",
                TargetFilters.creatureYouControl()
        ));

        // {R}{R}{R}{R}, {T}: Target creature you control can't be blocked this turn. Destroy it at
        // end of combat.
        addActivatedAbility(new ActivatedAbility(
                true, "{R}{R}{R}{R}",
                List.of(
                        new MakeCreatureUnblockableEffect(),
                        new DestroyTargetPermanentAtEndOfCombatEffect()
                ),
                "{R}{R}{R}{R}, {T}: Target creature you control can't be blocked this turn. "
                        + "Destroy it at end of combat.",
                TargetFilters.creatureYouControl()
        ));
    }
}
