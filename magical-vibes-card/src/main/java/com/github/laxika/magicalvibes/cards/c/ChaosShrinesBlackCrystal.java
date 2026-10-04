package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FIC", collectorNumber = "445")
public class ChaosShrinesBlackCrystal extends Card {

    public ChaosShrinesBlackCrystal() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new ExileTriggeringCreatureAndTrackWithSourceEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(
                        new ReturnCardExiledWithSourceToBattlefieldEffect(
                                new CardTypePredicate(CardType.CREATURE), false, null,
                                false, false, false, false, 0, false, false,
                                new EnterWithCountersEffect(CounterType.FINALITY, new Fixed(1))),
                        "Put a creature card exiled with Chaos Shrine's Black Crystal onto the battlefield with a finality counter on it?"));
    }
}
