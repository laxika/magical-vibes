package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ONS", collectorNumber = "213")
public class Insurrection extends Card {

    public Insurrection() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();

        // Untap all creatures and gain control of them until end of turn.
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES, creature));
        addEffect(EffectSlot.SPELL,
                new GainControlOfAllPermanentsMatchingEffect(creature, ControlDuration.END_OF_TURN));

        // They gain haste until end of turn.
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
    }
}
