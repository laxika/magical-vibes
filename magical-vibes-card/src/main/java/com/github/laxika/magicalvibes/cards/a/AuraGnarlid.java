package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithLessPowerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ROE", collectorNumber = "175")
public class AuraGnarlid extends Card {

    public AuraGnarlid() {
        // Creatures with power less than this creature's power can't block it.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithLessPowerEffect());

        // This creature gets +1/+1 for each Aura on the battlefield.
        PermanentCount aurasOnBattlefield = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.AURA), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(aurasOnBattlefield, aurasOnBattlefield));
    }
}
