package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostForTargetingSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "482")
@CardRegistration(set = "FIN", collectorNumber = "552")
public class CloudPlanetsChampion extends Card {

    public CloudPlanetsChampion() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllConditions(List.of(new ControllerTurn(), new Equipped())),
                new GrantKeywordEffect(Set.of(Keyword.DOUBLE_STRIKE, Keyword.INDESTRUCTIBLE), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ReduceActivatedAbilityCostForTargetingSourceEffect(2));
    }
}
