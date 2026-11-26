package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "220")
public class GrangerGuildmage extends Card {

    public GrangerGuildmage() {
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(SequenceEffect.simultaneous(
                        DealDamageEffect.toAnyTarget(1),
                        DealDamageEffect.to(Subjects.you(), 1)
                )),
                "{R}, {T}: Granger Guildmage deals 1 damage to any target and 1 damage to you."));

        addActivatedAbility(new ActivatedAbility(true, "{W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target())),
                "{W}, {T}: Target creature gains first strike until end of turn.",
                TargetFilters.creature()));
    }
}
