package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "117")
public class Abomination extends Card {

    public Abomination() {
        // Whenever this creature blocks or becomes blocked by a green or white creature,
        // destroy that creature at end of combat.
        PermanentColorInPredicate greenOrWhite =
                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE));
        addEffect(EffectSlot.ON_BLOCK, new DestroyCombatOpponentAtEndOfCombatEffect(greenOrWhite, false));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DestroyCombatOpponentAtEndOfCombatEffect(greenOrWhite, false),
                TriggerMode.PER_BLOCKER);
    }
}
