package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentManaValueSum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "1")
@CardRegistration(set = "FIN", collectorNumber = "356")
public class SummonBahamut extends Card {

    public SummonBahamut() {
        var nonlandPermanent = new PermanentNotPredicate(new PermanentIsLandPredicate());
        addTrigger(Triggers.chapter(1), new DestroyTargetPermanentEffect(nonlandPermanent));
        addTrigger(Triggers.chapter(2), new DestroyTargetPermanentEffect(nonlandPermanent));
        addTrigger(Triggers.chapter(3), new DrawCardEffect(2));
        addTrigger(Triggers.chapter(4), new DealDamageToPlayersEffect(
                new PermanentManaValueSum(
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                        CountScope.CONTROLLER),
                DamageRecipient.EACH_OPPONENT));
    }
}
