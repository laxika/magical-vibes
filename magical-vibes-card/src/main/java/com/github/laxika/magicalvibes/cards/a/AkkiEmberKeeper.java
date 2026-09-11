package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "130")
public class AkkiEmberKeeper extends Card {

    public AkkiEmberKeeper() {
        var spiritToken = new CreateTokenEffect("Spirit", 1, 1, null,
                List.of(CardSubtype.SPIRIT), Set.of(), Set.of());
        var modifiedNontoken = new PermanentAllOfPredicate(List.of(
                new PermanentIsModifiedPredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(modifiedNontoken, spiritToken));
        addEffect(EffectSlot.ON_DEATH,
                new TriggeringPermanentConditionalEffect(new PermanentIsModifiedPredicate(), spiritToken));
    }
}
