package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "252")
public class ZhaoRuthlessAdmiral extends Card {

    public ZhaoRuthlessAdmiral() {
        addEffect(EffectSlot.ON_ATTACK, new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 2));
        addTrigger(Triggers.sacrificed(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        new BoostAllOwnCreaturesEffect(1, 0)));
    }
}
