package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToSourceOrHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "1")
@CardRegistration(set = "PC2", collectorNumber = "3")
@CardRegistration(set = "PCA", collectorNumber = "3")
public class AuratouchedMage extends Card {

    public AuratouchedMage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryForAuraToBattlefieldAttachedToSourceOrHandEffect());
    }
}
