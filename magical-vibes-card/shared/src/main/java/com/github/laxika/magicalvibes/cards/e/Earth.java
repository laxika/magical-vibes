package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Earth — back half of Heaven // Earth.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Earth deals X damage to each
 * creature without flying.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Earth extends Card {

    public Earth() {
        // Earth deals X damage to each creature without flying.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                0, true, false,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));

        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{X}{R}{R}"));
    }
}
