package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.Earth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

/**
 * Heaven // Earth — front half (Heaven).
 * Instant — Heaven deals X damage to each creature with flying.
 * Back half (Earth) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "224")
@CardRegistration(set = "AKR", collectorNumber = "239")
public class HeavenEarth extends Card {

    public HeavenEarth() {
        setBackFaceCard(new Earth());

        // Heaven deals X damage to each creature with flying.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                0, true, false, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }

    @Override
    public String getBackFaceClassName() {
        return "Earth";
    }
}
