package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SCG", collectorNumber = "111")
@CardRegistration(set = "DD1", collectorNumber = "1")
@CardRegistration(set = "EVG", collectorNumber = "1")
public class AmbushCommander extends Card {

    public AmbushCommander() {
        addEffect(EffectSlot.STATIC, new AnimatePermanentsEffect(
                new Fixed(1), new Fixed(1), List.of(CardSubtype.ELF), Set.of(), null,
                Set.of(), GrantScope.ALL_PERMANENTS, EffectDuration.CONTINUOUS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                        new PermanentControlledBySourceControllerPredicate())), Set.of(CardColor.GREEN)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.ELF), "an Elf", false),
                        new BoostTargetCreatureEffect(3, 3)
                ),
                "{1}{G}, Sacrifice an Elf: Target creature gets +3/+3 until end of turn.",
                TargetFilters.creature()));
    }
}
