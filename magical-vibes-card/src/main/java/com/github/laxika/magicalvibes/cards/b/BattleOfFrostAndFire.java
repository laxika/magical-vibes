package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "204")
public class BattleOfFrostAndFire extends Card {

    public BattleOfFrostAndFire() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MassDamageEffect(
                4,
                false,
                true,
                true,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GIANT)),
                        new PermanentIsPlaneswalkerPredicate()))));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new ScryEffect(3));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                RegisterDelayedControllerSpellCastTriggerEffect.withStackEntryFilter(
                        new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(4)),
                        List.of(
                                new DrawCardEffect(2),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                        false));
    }
}
