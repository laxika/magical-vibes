package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "101")
public class MoonstoneHarbinger extends Card {

    public MoonstoneHarbinger() {
        PermanentHasSubtypePredicate bats = new PermanentHasSubtypePredicate(CardSubtype.BAT);
        SequenceEffect lifeChangeEffect = SequenceEffect.of(
                new BoostAllOwnCreaturesEffect(1, 0, bats),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES, bats));

        CardEffect ownTurnLifeChange = new ConditionalEffect(new ControllerTurn(), lifeChangeEffect);
        addTrigger(Triggers.youGainLife(), new OncePerTurnTriggerEffect(ownTurnLifeChange));
        addTrigger(Triggers.youLoseLife(), new OncePerTurnTriggerEffect(ownTurnLifeChange));
    }
}
