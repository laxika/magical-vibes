package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "140")
public class ShadowGuildmage extends Card {

    public ShadowGuildmage() {
        addActivatedAbility(new ActivatedAbility(true, "{U}",
                List.of(new PutTargetOnTopOfLibraryEffect()),
                "{U}, {T}: Put target creature you control on top of its owner's library.",
                TargetFilters.creatureYouControl()));

        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(
                        new DealDamageToAnyTargetEffect(1),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{R}, {T}: Shadow Guildmage deals 1 damage to any target and 1 damage to you."));
    }
}
