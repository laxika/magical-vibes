package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "164")
public class SmaugWickedWorm extends Card {

    public SmaugWickedWorm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofTreasureToken(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.OPPONENTS), true));
        addTrigger(Triggers.youCastSpell().paidWithTreasureMana(), new DrawCardEffect(), new LoseLifeEffect(1));
    }
}
