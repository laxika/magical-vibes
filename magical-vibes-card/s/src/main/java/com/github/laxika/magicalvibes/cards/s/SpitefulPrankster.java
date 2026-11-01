package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "801")
public class SpitefulPrankster extends Card {

    public SpitefulPrankster() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));
    }
}
