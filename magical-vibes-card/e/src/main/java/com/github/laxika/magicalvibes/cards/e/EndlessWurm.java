package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "249")
public class EndlessWurm extends Card {

    public EndlessWurm() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentIsEnchantmentPredicate(), "an enchantment"));
    }
}
