package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "661")
public class KingpinWilsonFisk extends Card {

    public KingpinWilsonFisk() {
        // "Whenever you sacrifice Kingpin or another creature" — no separate its dies trigger effect, so the
        // ally-sacrifice slot also covers Kingpin itself and both cases share one once-per-turn limit.
        addTrigger(Triggers.sacrificed(null, ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new OncePerTurnTriggerEffect(new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        CreateTokenEffect.ofTreasureToken(2)
                )));
    }
}
