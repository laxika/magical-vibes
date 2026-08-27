package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

@CardRegistration(set = "FIN", collectorNumber = "105")
@CardRegistration(set = "FIN", collectorNumber = "316")
@CardRegistration(set = "FIN", collectorNumber = "449")
public class KainTraitorousDragoon extends Card {

    public KainTraitorousDragoon() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));

        EventValue damage = new EventValue();
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer(
                        SequenceEffect.of(
                                new DrawCardEffect(damage),
                                CreateTokenEffect.ofTreasureToken(damage, true),
                                new LoseLifeEffect(damage, LoseLifeRecipient.CONTROLLER))));
    }
}
