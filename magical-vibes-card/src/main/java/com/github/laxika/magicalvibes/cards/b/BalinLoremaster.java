package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasEnduringStory;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyAndDealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "HOB", collectorNumber = "87")
public class BalinLoremaster extends Card {

    public BalinLoremaster() {
        addEffect(EffectSlot.STATIC, new StoriedEffect());
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DWARF),
                        new MayEffect(
                                new DiscardOwnHandThenDrawThatManyAndDealDamageEffect(
                                        new ControllerHasEnduringStory()),
                                "Discard your hand and draw that many cards?")));
    }
}
