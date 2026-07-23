package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "62")
public class BreathOfDreams extends Card {

    public BreathOfDreams() {
        // Cumulative upkeep {U}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{U}"));

        // Green creatures have "Cumulative upkeep {1}."
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CumulativeUpkeepEffect("{1}"),
                GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}
