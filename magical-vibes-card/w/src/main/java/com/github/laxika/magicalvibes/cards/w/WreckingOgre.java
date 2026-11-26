package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "112")
public class WreckingOgre extends Card {

    public WreckingOgre() {
        // Bloodrush — {3}{R}{R}, Discard this card: Target attacking creature gets +3/+3 and gains double strike until end of turn.
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(false, "{3}{R}{R}",
                List.of(BoostEffect.target(3, 3),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, Subjects.target())),
                "Bloodrush — {3}{R}{R}, Discard this card: Target attacking creature gets +3/+3 and gains double strike until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
