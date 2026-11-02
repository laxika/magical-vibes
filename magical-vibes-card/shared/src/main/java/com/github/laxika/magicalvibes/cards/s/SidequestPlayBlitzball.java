package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WorldChampionCelestialWeapon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerDealtCombatDamageAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfAndAttachToCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "158")
public class SidequestPlayBlitzball extends Card {

    public SidequestPlayBlitzball() {
        setBackFaceCard(new WorldChampionCelestialWeapon());

        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new BoostTargetCreatureEffect(2, 0));
        addTrigger(Triggers.endOfCombat(),
                new ConditionalEffect(
                        new AllConditions(List.of(
                                new ControllerTurn(),
                                new AnyPlayerDealtCombatDamageAtLeastThisTurn(6)
                        )),
                        new TransformSelfAndAttachToCreatureYouControlEffect()
                ));
    }

    @Override
    public String getBackFaceClassName() {
        return "WorldChampionCelestialWeapon";
    }
}
