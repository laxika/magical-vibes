package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "19")
public class ImperialSubduer extends Card {

    public ImperialSubduer() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SAMURAI, CardSubtype.WARRIOR)),
                                new ConditionalEffect(new AttacksAlone(),
                                        new TapPermanentsEffect(TapUntapScope.TARGET))));
    }
}
