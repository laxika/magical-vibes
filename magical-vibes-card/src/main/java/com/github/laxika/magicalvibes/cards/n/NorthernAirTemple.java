package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "111")
public class NorthernAirTemple extends Card {

    public NorthernAirTemple() {
        PermanentCount shrineCount = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new LoseLifeEffect(shrineCount, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(shrineCount)));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SHRINE),
                        SequenceEffect.of(
                                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(1))));
    }
}
