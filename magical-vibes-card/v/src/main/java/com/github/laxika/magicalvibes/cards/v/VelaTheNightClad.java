package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PC2", collectorNumber = "107")
@CardRegistration(set = "PCA", collectorNumber = "107")
@CardRegistration(set = "C17", collectorNumber = "201")
@CardRegistration(set = "SCD", collectorNumber = "256")
public class VelaTheNightClad extends Card {

    public VelaTheNightClad() {
        addStaticEffect(new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.OWN_CREATURES));

        LoseLifeEffect loseLife = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.CREATURE,
                                ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(loseLife))));
    }
}
