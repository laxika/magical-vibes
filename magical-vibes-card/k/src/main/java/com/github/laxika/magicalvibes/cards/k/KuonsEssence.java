package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class KuonsEssence extends Card {

    public KuonsEssence() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new SacrificePermanentsEffect(
                1,
                new PermanentIsCreaturePredicate(),
                SacrificeRecipient.ACTIVE_PLAYER));
    }
}
