package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordToSelfEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "76")
public class IllusionaryPresence extends Card {

    public IllusionaryPresence() {
        // Cumulative upkeep {U}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{U}"));

        // At the beginning of your upkeep, choose a land type. This creature gains landwalk of the
        // chosen type until end of turn.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new GrantChosenKeywordToSelfEffect(
                List.of(Keyword.PLAINSWALK, Keyword.ISLANDWALK, Keyword.SWAMPWALK,
                        Keyword.MOUNTAINWALK, Keyword.FORESTWALK)));
    }
}
