package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "220")
public class GrangerGuildmage extends Card {

    public GrangerGuildmage() {
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(
                        new DealDamageToAnyTargetEffect(1),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{R}, {T}: Granger Guildmage deals 1 damage to any target and 1 damage to you."));

        addActivatedAbility(new ActivatedAbility(true, "{W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                "{W}, {T}: Target creature gains first strike until end of turn.",
                TargetFilters.creature()));
    }
}
