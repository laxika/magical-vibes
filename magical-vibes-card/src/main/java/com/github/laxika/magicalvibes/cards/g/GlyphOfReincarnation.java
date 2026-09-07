package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "LEG", collectorNumber = "189")
public class GlyphOfReincarnation extends Card {

    public GlyphOfReincarnation() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.AFTER_COMBAT);
        target(new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL), "Target must be a Wall"))
                .addEffect(EffectSlot.SPELL, new DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect());
    }
}
