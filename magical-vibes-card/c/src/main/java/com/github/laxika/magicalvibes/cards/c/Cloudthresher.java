package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "202")
@CardRegistration(set = "DDR", collectorNumber = "6")
@CardRegistration(set = "C15", collectorNumber = "179")
@CardRegistration(set = "CMA", collectorNumber = "96")
public class Cloudthresher extends Card {

    public Cloudthresher() {
        // Flash and Reach are auto-loaded from Scryfall keywords.
        // Evoke {2}{G}{G}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{G}{G}"))));
        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());

        // When this creature enters, it deals 2 damage to each creature with flying and each player.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MassDamageEffect(2, false, true, new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
