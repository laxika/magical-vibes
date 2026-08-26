package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentManaValueSum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "FIN", collectorNumber = "1")
public class SummonBahamut extends Card {

    public SummonBahamut() {
        var nonlandPermanent = new PermanentNotPredicate(new PermanentIsLandPredicate());
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DestroyTargetPermanentEffect(nonlandPermanent));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DestroyTargetPermanentEffect(nonlandPermanent));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new DrawCardEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new DealDamageToPlayersEffect(
                new PermanentManaValueSum(
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                        CountScope.CONTROLLER),
                DamageRecipient.EACH_OPPONENT));
    }
}
