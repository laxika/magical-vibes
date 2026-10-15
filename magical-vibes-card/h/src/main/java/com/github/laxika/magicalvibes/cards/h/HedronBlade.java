package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CombatOpponentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;

@CardRegistration(set = "BFZ", collectorNumber = "224")
public class HedronBlade extends Card {

    public HedronBlade() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new CombatOpponentConditionalEffect(
                        new PermanentIsColorlessPredicate(),
                        new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, Keyword.DEATHTOUCH)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
