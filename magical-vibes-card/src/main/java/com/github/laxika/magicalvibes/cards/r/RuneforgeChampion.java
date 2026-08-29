package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "KHM", collectorNumber = "26")
public class RuneforgeChampion extends Card {

    public RuneforgeChampion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryAndOrGraveyardForCardToHandEffect(
                        new CardSubtypePredicate(CardSubtype.RUNE)),
                "Search your library and/or graveyard for a Rune card?"));
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect(
                "{1}", new CardSubtypePredicate(CardSubtype.RUNE)));
    }
}
