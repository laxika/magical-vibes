package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "109")
public class MobRule extends Card {

    public MobRule() {
        PermanentPredicate largeCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ));
        PermanentPredicate smallCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(3)
        ));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                mode("Gain control of all creatures with power 4 or greater", largeCreatures),
                mode("Gain control of all creatures with power 3 or less", smallCreatures)
        )));
    }

    private static ChooseOneEffect.ChooseOneOption mode(String label, PermanentPredicate filter) {
        return new ChooseOneEffect.ChooseOneOption(label, List.of(
                new GainControlOfAllPermanentsMatchingEffect(filter, ControlDuration.END_OF_TURN),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, filter),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES, filter)
        ));
    }
}
