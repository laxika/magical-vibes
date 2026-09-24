package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2252")
@CardRegistration(set = "SPG", collectorNumber = "13")
public class KalamaxTheStormsire extends Card {

    public KalamaxTheStormsire() {
        CardTypePredicate instant = new CardTypePredicate(CardType.INSTANT);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopyControllerCastSpellOnSpellCastEffect(
                        instant, null, null, null, null, null,
                        new SourceIsTapped(), false, null, false, true,
                        false, false, null, List.of(instant), null));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL,
                new SpellCopyTriggerEffect(instant,
                        List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
