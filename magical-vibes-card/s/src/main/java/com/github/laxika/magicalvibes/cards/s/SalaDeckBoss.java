package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasExhaustAbilityPredicate;

@CardRegistration(set = "YDFT", collectorNumber = "25")
public class SalaDeckBoss extends Card {

    public SalaDeckBoss() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasExhaustAbilityPredicate()));

        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect(
                        null, null, false, false, null, false, true));
    }
}
