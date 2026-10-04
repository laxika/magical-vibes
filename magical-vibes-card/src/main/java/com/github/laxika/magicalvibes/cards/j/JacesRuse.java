package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "273")
public class JacesRuse extends Card {

    public JacesRuse() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        addEffect(EffectSlot.SPELL, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Jace, Arcane Strategist"),
                "Search your library and/or graveyard for a card named Jace, Arcane Strategist?"
        ));
    }
}
