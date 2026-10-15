package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerSurveiledThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;

@CardRegistration(set = "GRN", collectorNumber = "164")
public class DarkbladeAgent extends Card {

    public DarkbladeAgent() {
        ControllerSurveiledThisTurn surveiledThisTurn = new ControllerSurveiledThisTurn();
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                surveiledThisTurn,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                surveiledThisTurn,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new DrawCardEffect(1),
                        GrantScope.SELF)));
    }
}
