package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "47")
public class Dystopia extends Card {

    public Dystopia() {
        // Cumulative upkeep—Pay 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.life(1));

        // At the beginning of each player's upkeep, that player sacrifices a green or white
        // permanent of their choice. EACH_UPKEEP_TRIGGERED sets the active player as the target
        // that sacrifices.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE)),
                SacrificeRecipient.TARGET_PLAYER));
    }
}
