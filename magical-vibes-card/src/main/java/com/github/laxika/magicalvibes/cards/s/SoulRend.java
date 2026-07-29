package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "144")
public class SoulRend extends Card {

    public SoulRend() {
        // Destroy target creature if it's white. A creature destroyed this way can't be
        // regenerated. Any creature is a legal target; the colour check happens at resolution.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetPermanentMatches(new PermanentColorInPredicate(Set.of(CardColor.WHITE))),
                new DestroyTargetPermanentEffect(true)));

        // Draw a card at the beginning of the next turn's upkeep.
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
