package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleReferencedPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "11")
public class DragonsoulProdigy extends Card {

    public DragonsoulProdigy() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.OMEN)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                ConjureDuplicateOntoBattlefieldEffect.of(Subjects.triggeringSpell()),
                new GrantKeywordEffect(Keyword.HASTE, Subjects.createdThisResolution(), Duration.PERMANENT),
                RegisterDelayedStepTriggerEffect.aboutCreated(Triggers.endStep(ActivePlayer.ANY),
                        new ShuffleReferencedPermanentIntoLibraryEffect(Subjects.createdThisResolution())));
    }
}
