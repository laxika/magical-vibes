package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Authority — back half of Appeal // Authority.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Tap up to two target creatures
 * your opponents control. Creatures you control gain vigilance until end of turn.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Authority extends Card {

    public Authority() {
        // Tap up to two target creatures your opponents control.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be a creature an opponent controls"
        ), 0, 2)
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));

        // Creatures you control gain vigilance until end of turn.
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));

        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{1}{W}"));
    }
}
