package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "M14", collectorNumber = "50")
public class DismissIntoDream extends Card {

    public DismissIntoDream() {
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.ILLUSION, GrantScope.OPPONENT_CREATURES));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                  EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                  new SacrificeSelfEffect(),
                  GrantScope.OPPONENT_CREATURES));
    }
}
