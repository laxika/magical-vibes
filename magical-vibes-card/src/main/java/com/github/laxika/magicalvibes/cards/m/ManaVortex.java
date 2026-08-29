package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.Collection;
import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "31")
public class ManaVortex extends Card {

    public ManaVortex() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new CounterUnlessSacrificesEffect(new PermanentIsLandPredicate(), "land"));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.ACTIVE_PLAYER));
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.playerBattlefields.values().stream()
                        .flatMap(Collection::stream)
                        .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND)),
                List.of(new SacrificeSelfEffect()),
                "Mana Vortex's state-triggered ability"));
    }
}
