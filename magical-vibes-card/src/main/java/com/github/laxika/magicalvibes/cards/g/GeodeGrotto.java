package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

public class GeodeGrotto extends Card {

    public GeodeGrotto() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        PermanentCount artifacts = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new BoostTargetCreatureEffect(artifacts, new Fixed(0)),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
                ),
                "{2}{R}, {T}: Until end of turn, target creature gains haste and gets +X/+0, where X is the number of artifacts you control. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
