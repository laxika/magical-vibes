package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "61")
public class NemesisTrap extends Card {

    public NemesisTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{B}{B}")),
                new AnyPlayerControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new PermanentIsAttackingPredicate()))),
                false));

        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentAndCreateTokenCopyEffect());
    }
}
