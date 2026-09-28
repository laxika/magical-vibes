package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MB2", collectorNumber = "259")
public class OracleOfTheAlpha extends Card {

    public OracleOfTheAlpha() {
        // When Oracle of the Alpha enters the battlefield, conjure the Power Nine into your library,
        // then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ConjureCardNamedIntoLibraryEffect("Black Lotus", 1),
                new ConjureCardNamedIntoLibraryEffect("Mox Pearl", 1),
                new ConjureCardNamedIntoLibraryEffect("Mox Sapphire", 1),
                new ConjureCardNamedIntoLibraryEffect("Mox Jet", 1),
                new ConjureCardNamedIntoLibraryEffect("Mox Ruby", 1),
                new ConjureCardNamedIntoLibraryEffect("Mox Emerald", 1),
                new ConjureCardNamedIntoLibraryEffect("Ancestral Recall", 1),
                new ConjureCardNamedIntoLibraryEffect("Time Walk", 1),
                new ConjureCardNamedIntoLibraryEffect("Timetwister", 1)
        ));

        // Whenever Oracle of the Alpha attacks, scry 1.
        addEffect(EffectSlot.ON_ATTACK, new ScryEffect(1));
    }
}
