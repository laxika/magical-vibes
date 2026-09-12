package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "12")
@CardRegistration(set = "VMA", collectorNumber = "23")
public class DeftbladeElite extends Card {

    public DeftbladeElite() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate()
                )),
                "Target must be a creature defending player controls"), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.TARGET),
                                new MustBlockSourceEffect(null)),
                        "Have target creature defending player controls untap and block Deftblade Elite if able?"
                ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        PreventDamageEffect.allCombatToSelf(),
                        PreventDamageEffect.allCombatBySelf()
                ),
                "{1}{W}: Prevent all combat damage that would be dealt to and dealt by this creature this turn."
        ));
    }
}
