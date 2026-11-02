package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "105")
@CardRegistration(set = "FIN", collectorNumber = "316")
@CardRegistration(set = "FIN", collectorNumber = "449")
public class KainTraitorousDragoon extends Card {

    public KainTraitorousDragoon() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));

        EventValue damage = new EventValue();
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer(
                        SequenceEffect.of(
                                new DrawCardEffect(damage),
                                CreateTokenEffect.ofTreasureToken(damage, true),
                                new LoseLifeEffect(damage, LoseLifeRecipient.CONTROLLER))));
    }
}
