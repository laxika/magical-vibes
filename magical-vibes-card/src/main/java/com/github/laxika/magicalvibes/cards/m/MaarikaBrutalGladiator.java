package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect;

@CardRegistration(set = "SLD", collectorNumber = "435")
public class MaarikaBrutalGladiator extends Card {

    public MaarikaBrutalGladiator() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new ControllerTurn(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect());
    }
}
