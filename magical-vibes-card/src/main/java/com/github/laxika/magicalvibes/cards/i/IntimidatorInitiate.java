package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "96")
public class IntimidatorInitiate extends Card {

    public IntimidatorInitiate() {
        // Whenever a player casts a red spell, you may pay {1}. If you do, target creature can't block this turn.
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                        new SpellCastTriggerEffect(
                                new CardColorPredicate(CardColor.RED),
                                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                                "{1}"),
                        "Pay {1} to make target creature unable to block this turn?"));
    }
}
