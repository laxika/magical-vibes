package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "111")
public class GorgonRecluse extends Card {

    public GorgonRecluse() {
        // Whenever this creature blocks or becomes blocked by a nonblack creature,
        // destroy that creature at end of combat.
        PermanentNotPredicate nonblack = new PermanentNotPredicate(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)));
        addEffect(EffectSlot.ON_BLOCK, new DestroyCombatOpponentAtEndOfCombatEffect(nonblack, false));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DestroyCombatOpponentAtEndOfCombatEffect(nonblack, false), TriggerMode.PER_BLOCKER);

        // Madness {B}{B}
        addCastingOption(new MadnessCast("{B}{B}"));
    }
}
