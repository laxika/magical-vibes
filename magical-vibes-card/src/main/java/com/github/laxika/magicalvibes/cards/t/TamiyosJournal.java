package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "272")
public class TamiyosJournal extends Card {

    public TamiyosJournal() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CreateTokenEffect.ofClueToken(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.CLUE)),
                        new SearchLibraryEffect()
                ),
                "{T}, Sacrifice three Clues: Search your library for a card, put that card into your hand, then shuffle."
        ));
    }
}
