package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "116")
public class SpectralSnatcher extends Card {

    public SpectralSnatcher() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessDiscardsEffect());
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.SWAMP))),
                "Swampcycling {2} ({2}, Discard this card: Search your library for a Swamp card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
