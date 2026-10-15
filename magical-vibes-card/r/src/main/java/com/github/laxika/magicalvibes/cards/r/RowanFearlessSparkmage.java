package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "304")
public class RowanFearlessSparkmage extends Card {

    public RowanFearlessSparkmage() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new BoostTargetCreatureEffect(3, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)
                ),
                "+1: Up to one target creature gets +3/+0 and gains first strike until end of turn.",
                null,
                +1,
                null,
                null,
                List.of(TargetFilters.creature()),
                0,
                1
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DealDamageToEachTargetEffect(new Fixed(1), null, new PermanentIsCreaturePredicate()),
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET)
                ),
                "\u22122: Rowan deals 1 damage to each of up to two target creatures. Those creatures can't block this turn.",
                null,
                -2,
                null,
                null,
                List.of(TargetFilters.creature(), TargetFilters.creature()),
                0,
                2
        ));

        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(
                        new GainControlOfAllPermanentsMatchingEffect(creature, ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, creature),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES)
                ),
                "\u22129: Gain control of all creatures until end of turn. Untap them. They gain haste until end of turn."
        ));
    }
}
