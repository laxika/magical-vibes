package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "75")
public class TreacherousVampire extends Card {

    public TreacherousVampire() {
        ForcedCostOrElseEffect sacrificeUnlessExile = new ForcedCostOrElseEffect(
                new ExileCardFromGraveyardCost((CardType) null),
                List.of(new SacrificeSelfEffect()),
                true);
        addEffect(EffectSlot.ON_ATTACK, sacrificeUnlessExile);
        addEffect(EffectSlot.ON_BLOCK, sacrificeUnlessExile);

        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new GrantTriggeredAbilityEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(6), GrantScope.SELF)));
    }
}
