package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Stalking Vampire — back face of Screeching Bat.
 * 5/5 Vampire.
 * At the beginning of your upkeep, you may pay {2}{B}{B}. If you do, transform Stalking Vampire.
 */
public class StalkingVampire extends Card {

    public StalkingVampire() {
        // At the beginning of your upkeep, you may pay {2}{B}{B}. If you do, transform Stalking Vampire.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayPayManaEffect("{2}{B}{B}", new TransformSelfEffect(),
                        "Pay {2}{B}{B} to transform Stalking Vampire?"));
    }
}
