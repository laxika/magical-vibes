package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "40K", collectorNumber = "6")
@CardRegistration(set = "40K", collectorNumber = "172")
public class BelakorTheDarkMaster extends Card {

    public BelakorTheDarkMaster() {
        PermanentCount demons = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.DEMON), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(demons));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LoseLifeEffect(demons, LoseLifeRecipient.CONTROLLER));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DEMON),
                        new DealDamageToAnyTargetEffect(new SourcePower())));
    }
}
