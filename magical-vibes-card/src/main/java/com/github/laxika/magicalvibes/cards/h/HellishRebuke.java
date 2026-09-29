package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AFC", collectorNumber = "26")
public class HellishRebuke extends Card {

    public HellishRebuke() {
        addEffect(EffectSlot.SPELL, new GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect(
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_DAMAGE_TO_OPPONENT,
                        SequenceEffect.of(new SacrificeSelfEffect(), new LoseLifeEffect(2)),
                        GrantScope.SELF)));
    }
}
