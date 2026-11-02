package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RageOfWinter;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "61")
@CardRegistration(set = "ELD", collectorNumber = "285")
public class QueenOfIce extends Card {

    public QueenOfIce() {
        setBackFaceCard(new RageOfWinter());
        addCastingOption(new AdventureCast("{1}{U}"));

        // Whenever this creature deals combat damage to a creature, tap that creature. It doesn't untap
        // during its controller's next untap step. The damaged creature is baked as targetId by
        // Triggers.dealsCombatDamageToCreature() (non-targeting), so both halves act on it.
        addTrigger(Triggers.dealsCombatDamageToCreature(), SequenceEffect.of(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }

    @Override
    public String getBackFaceClassName() {
        return "RageOfWinter";
    }
}
