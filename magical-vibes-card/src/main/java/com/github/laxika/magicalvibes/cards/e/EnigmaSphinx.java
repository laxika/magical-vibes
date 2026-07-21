package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;

@CardRegistration(set = "ARB", collectorNumber = "106")
public class EnigmaSphinx extends Card {

    public EnigmaSphinx() {
        // Flying is auto-loaded from Scryfall.

        // When this creature is put into your graveyard from the battlefield, put it into
        // your library third from the top (0-indexed position 2).
        addEffect(EffectSlot.ON_DEATH, new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2));

        // Cascade: when you cast this spell, dig the library until a nonland card with lesser
        // mana value, may cast it for free, rest to the bottom in a random order.
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
