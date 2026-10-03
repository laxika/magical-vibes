package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "12")
@CardRegistration(set = "BRC", collectorNumber = "59")
public class BlastFurnaceHellkite extends Card {

    public BlastFurnaceHellkite() {
        addCastingOption(AlternateHandCast.offering(List.of(
                new ManaCastingCost("{7}{R}{R}"),
                new SacrificePermanentsCost(1, new PermanentIsArtifactPredicate())
        )));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE,
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentIsAttackingOpponentOfSourceControllerPredicate()));
    }
}
