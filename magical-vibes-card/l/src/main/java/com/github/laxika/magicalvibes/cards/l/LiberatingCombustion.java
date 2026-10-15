package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "267")
public class LiberatingCombustion extends Card {

    public LiberatingCombustion() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(6))
                .addEffect(EffectSlot.SPELL, new MayEffect(
                        new SearchLibraryAndOrGraveyardForCardToHandEffect(
                                new CardNamedPredicate("Chandra, Pyrogenius")),
                        "Search your library and/or graveyard for a card named Chandra, Pyrogenius?"));
    }
}
