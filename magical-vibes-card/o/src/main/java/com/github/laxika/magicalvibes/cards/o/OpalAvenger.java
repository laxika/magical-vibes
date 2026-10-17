package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceMatchesPermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "15")
public class OpalAvenger extends Card {

    public OpalAvenger() {
        // "if this permanent is an enchantment" is an intervening-if (CR 603.4), so it is re-checked as the
        // ability resolves; the life-total trigger condition is not.
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) <= 10,
                new PermanentIsEnchantmentPredicate(), null, 0, null,
                List.of(new ConditionalEffect(
                        new SourceMatchesPermanentPredicate(new PermanentIsEnchantmentPredicate()),
                        new BecomeCreatureEffect(3, 5, CardSubtype.SOLDIER))),
                "Opal Avenger's state-triggered ability"
        ));
    }
}
