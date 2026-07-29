package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EruptingDreadwolf;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "171")
public class SmolderingWerewolf extends Card {

    public SmolderingWerewolf() {
        setBackFaceCard(new EruptingDreadwolf());

        // When this creature enters, it deals 1 damage to each of up to two target creatures.
        target(TargetFilters.creature(), 0, 2).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(1));

        // {4}{R}{R}: Transform this creature.
        addActivatedAbility(new ActivatedAbility(
                false, "{4}{R}{R}",
                List.of(new TransformSelfEffect()),
                "{4}{R}{R}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "EruptingDreadwolf";
    }
}
