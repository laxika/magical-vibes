package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "VIS", collectorNumber = "83")
public class HeatWave extends Card {

    public HeatWave() {
        // Cumulative upkeep {R}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{R}"));

        // Blue creatures can't block creatures you control.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                new PermanentControlledBySourceControllerPredicate(),
                "Blue creatures can't block creatures you control"));

        // Nonblue creatures can't block creatures you control unless their controller pays 1 life
        // for each blocking creature they control.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesUnlessPaysLifeEffect(
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLUE))),
                new PermanentControlledBySourceControllerPredicate(),
                1,
                "Nonblue creatures can't block creatures you control unless their controller pays 1 life"));
    }
}
