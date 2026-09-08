package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MSH", collectorNumber = "202")
public class AresGodOfWar extends Card {

    public AresGodOfWar() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(), new ReturnTriggeringCardToOwnerHandEffect()));

        addEffect(EffectSlot.ON_DEATH, new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(), new ReturnSourceCardFromGraveyardToOwnerHandEffect()));
    }
}
