package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "106")
@CardRegistration(set = "PC2", collectorNumber = "89")
@CardRegistration(set = "PCA", collectorNumber = "89")
@CardRegistration(set = "C18", collectorNumber = "178")
public class EnigmaSphinx extends Card {

    public EnigmaSphinx() {
        // Flying is auto-loaded from Scryfall.

        // When this creature is put into your graveyard from the battlefield, put it into
        // your library third from the top (0-indexed position 2).
        // It goes to its owner's graveyard, so only the owner's ability can trigger.
        addTrigger(Triggers.dies(new PermanentOwnedBySourceControllerPredicate(), ObjectRelation.THIS),
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2));

        // Cascade: when you cast this spell, dig the library until a nonland card with lesser
        // mana value, may cast it for free, rest to the bottom in a random order.
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
    }
}
