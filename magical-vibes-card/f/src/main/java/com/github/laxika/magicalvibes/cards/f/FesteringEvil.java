package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "68")
public class FesteringEvil extends Card {

    public FesteringEvil() {
        // At the beginning of your upkeep, this enchantment deals 1 damage to each creature and each player.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MassDamageEffect(1, true));

        // {B}{B}, Sacrifice this enchantment: It deals 3 damage to each creature and each player.
        addActivatedAbility(new ActivatedAbility(false, "{B}{B}",
                List.of(new SacrificeSelfCost(), new MassDamageEffect(3, true)),
                "{B}{B}, Sacrifice this enchantment: It deals 3 damage to each creature and each player."));
    }
}
