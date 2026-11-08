package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "224")
public class HedronBlade extends Card {

    public HedronBlade() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        // "Becomes blocked by one or more colorless creatures": once, if a blocker matches the filter.
        addTrigger(Triggers.becomesBlocked().withFilter(new PermanentIsColorlessPredicate()),
                new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, Keyword.DEATHTOUCH));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
