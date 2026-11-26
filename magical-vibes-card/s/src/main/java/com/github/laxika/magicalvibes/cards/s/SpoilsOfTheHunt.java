package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.TreasureManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "205")
public class SpoilsOfTheHunt extends Card {

    public SpoilsOfTheHunt() {
        SpellTarget sourceTarget = target(TargetFilters.creatureYouControl());
        sourceTarget.addSpellEffect(BoostEffect.target(new TreasureManaSpentToCast(), new Fixed(0)));

        SpellTarget victimTarget = target(TargetFilters.creatureAnOpponentControls());
        victimTarget.addSpellEffect(DealDamageEffect.toAnyTargetInGroup(new SourcePower(), 1)
                .dealtBy(Subjects.target(0)));
    }
}
