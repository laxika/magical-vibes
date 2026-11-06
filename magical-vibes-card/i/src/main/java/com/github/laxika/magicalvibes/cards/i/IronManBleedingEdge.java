package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "MSC", collectorNumber = "626")
public class IronManBleedingEdge extends Card {

    public IronManBleedingEdge() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(
                        new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens()
                                .withoutSupertypes(Set.of(CardSupertype.LEGENDARY)),
                        "Copy that spell?"));
    }
}
