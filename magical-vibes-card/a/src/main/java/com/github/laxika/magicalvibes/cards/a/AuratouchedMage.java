package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardCouldEnchantPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "1")
@CardRegistration(set = "PC2", collectorNumber = "3")
@CardRegistration(set = "PCA", collectorNumber = "3")
public class AuratouchedMage extends Card {

    public AuratouchedMage() {
        // "An Aura card that could enchant it": the Mage as it is, or as it last existed on the battlefield (its ruling).
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new SearchLibraryEffect(new CardCouldEnchantPredicate(Subjects.source()),
                        LibrarySearchDestination.REVEAL_ONLY, null, false),
                new ConditionalEffect(new SourceIsOnBattlefield(),
                        new PutCardsOntoBattlefieldEffect(Subjects.foundThisWay()).attachedTo(Subjects.source())),
                new ConditionalEffect(new NotCondition(new SourceIsOnBattlefield()),
                        new PutCardsIntoHandEffect(Subjects.foundThisWay())),
                new ShuffleLibraryEffect(false)));
    }
}
