package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "42")
public class MantaRay extends Card {

    public MantaRay() {
        // "This creature can't attack unless defending player controls an Island."
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));

        // "This creature can't be blocked except by blue creatures."
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLUE)), "blue creatures"));

        // "When you control no Islands, sacrifice this creature." —
        // State-triggered ability (MTG rule 603.8).
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                    if (battlefield == null) return true;
                    return battlefield.stream()
                            .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.ISLAND));
                },
                List.of(new SacrificeSelfEffect()),
                "Manta Ray's state-triggered ability"
        ));
    }
}
