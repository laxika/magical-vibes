package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Chittering Host — meld result of Graf Rats and Midnight Scavengers.
 * Haste and Menace are loaded from Scryfall.
 */
@CardRegistration(set = "INR", collectorNumber = "123b")
public class ChitteringHost extends Card {

    public ChitteringHost() {
        // When this creature enters, other creatures you control get +1/+0 and gain menace until end of turn.
        var otherOwnCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(1, 0, otherOwnCreatures));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES, otherOwnCreatures));
    }
}
