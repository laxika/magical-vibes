package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "12")
public class BreachingLeviathan extends Card {

    private static final PermanentPredicate NONBLUE = new PermanentNotPredicate(
            new PermanentColorInPredicate(Set.of(CardColor.BLUE)));

    public BreachingLeviathan() {
        // If this was cast from hand, tap all nonblue creatures. Those creatures don't untap during
        // their controllers' next untap steps.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastFromZone(Zone.HAND),
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.ALL_CREATURES, NONBLUE),
                        new SkipNextUntapEffect(TapUntapScope.ALL_CREATURES, NONBLUE))));
    }
}
