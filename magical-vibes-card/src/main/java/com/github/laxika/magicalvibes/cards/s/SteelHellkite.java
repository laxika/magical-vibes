package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "205")
public class SteelHellkite extends Card {

    public SteelHellkite() {
        // {2}: Steel Hellkite gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new BoostSelfEffect(1, 0)),
                "{2}: Steel Hellkite gets +1/+0 until end of turn."));

        // {X}: Destroy each nonland permanent with mana value X whose controller was dealt
        // combat damage by Steel Hellkite this turn. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(false, "{X}",
                List.of(new DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect()),
                "{X}: Destroy each nonland permanent with mana value X whose controller was dealt combat damage by Steel Hellkite this turn. Activate only once each turn.",
                1));
    }
}
