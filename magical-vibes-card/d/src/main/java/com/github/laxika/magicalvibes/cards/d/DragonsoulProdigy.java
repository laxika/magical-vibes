package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "11")
public class DragonsoulProdigy extends Card {

    public DragonsoulProdigy() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.OMEN)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect());
    }
}
