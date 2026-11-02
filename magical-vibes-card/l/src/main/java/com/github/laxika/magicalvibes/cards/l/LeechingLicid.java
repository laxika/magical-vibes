package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "141")
public class LeechingLicid extends Card {

    public LeechingLicid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new LicidBecomeAuraEffect("{B}")),
                "{B}, {T}: This creature loses this ability and becomes an Aura enchantment with enchant"
                        + " creature. Attach it to target creature. You may pay {B} to end this effect.",
                TargetFilters.creature()
        ));
        // At the beginning of the upkeep of enchanted creature's controller, this creature deals 1 damage to that player.
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
