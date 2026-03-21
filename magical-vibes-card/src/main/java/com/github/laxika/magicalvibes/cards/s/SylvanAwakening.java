package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

/**
 * Sylvan Awakening — {2}{G} Sorcery
 *
 * Until your next turn, all lands you control become 2/2 Elemental creatures with reach,
 * indestructible, and haste. They're still lands.
 */
@CardRegistration(set = "DOM", collectorNumber = "183")
public class SylvanAwakening extends Card {

    public SylvanAwakening() {
        addEffect(EffectSlot.SPELL, new AnimateLandEffect(
                2, 2,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.REACH, Keyword.INDESTRUCTIBLE, Keyword.HASTE),
                null, Set.of(),
                GrantScope.OWN_LANDS, EffectDuration.UNTIL_YOUR_NEXT_TURN
        ));
    }
}
