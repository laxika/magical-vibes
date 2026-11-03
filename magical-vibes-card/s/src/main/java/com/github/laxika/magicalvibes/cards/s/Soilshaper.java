package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "243")
public class Soilshaper extends Card {

    public Soilshaper() {
        // Whenever you cast a Spirit or Arcane spell, target land becomes a 3/3 creature until
        // end of turn. It's still a land.
        target(TargetFilters.land()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE)))),
                new AnimatePermanentsEffect(
                        3, 3, List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN));
    }
}
