package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "76")
@CardRegistration(set = "ECL", collectorNumber = "288")
public class SyggWanderwineWisdom extends Card {

    public SyggWanderwineWisdom() {
        setBackFaceCard(new SyggWanderbrineShield());

        addStaticEffect(new CantBeBlockedEffect());

        GrantEffectToTargetUntilEndOfTurnEffect combatDamageDraw =
                new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayerOrPlaneswalker(),
                                new DrawCardEffect(1)));

        // Whenever this creature enters or transforms into Sygg, Wanderwine Wisdom, target creature
        // gains "Whenever this creature deals combat damage to a player or planeswalker, draw a card"
        // until end of turn.
        target(TargetFilters.creature());
        addTrigger(Triggers.enters(ObjectRelation.THIS), combatDamageDraw);
        addTrigger(Triggers.transformsToFrontFace(ObjectRelation.THIS), combatDamageDraw);

        // At the beginning of your first main phase, you may pay {W}. If you do, transform Sygg.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new MayPayManaEffect("{W}", new TransformSelfEffect(), "Pay {W} to transform Sygg?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SyggWanderbrineShield";
    }
}
