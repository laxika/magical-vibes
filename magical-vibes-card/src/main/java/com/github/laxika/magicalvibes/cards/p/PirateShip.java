package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "91")
@CardRegistration(set = "5ED", collectorNumber = "109")
public class PirateShip extends Card {

    public PirateShip() {
        // "This creature can't attack unless defending player controls an Island."
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));

        // "{T}: This creature deals 1 damage to any target."
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Pirate Ship deals 1 damage to any target."));

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
                "Pirate Ship's state-triggered ability"
        ));
    }
}
