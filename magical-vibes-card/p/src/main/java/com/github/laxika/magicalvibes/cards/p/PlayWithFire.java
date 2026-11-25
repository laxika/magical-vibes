package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

@CardRegistration(set = "MID", collectorNumber = "154")
@CardRegistration(set = "DBL", collectorNumber = "154")
public class PlayWithFire extends Card {

    public PlayWithFire() {
        addSpellEffect(SequenceEffect.of(
                        DealDamageEffect.toAnyTarget(2),
                        new ConditionalEffect(HappenedThisWay.toAPlayer(StepKind.DEAL_DAMAGE, PlayerRelation.ANY),
                                new ScryEffect(1))));
    }
}
