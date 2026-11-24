package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect.RedirectedDamage;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "18")
public class TurnTheTables extends Card {

    public TurnTheTables() {
        target(TargetFilters.attackingCreature())
                .addSpellEffect(RedirectYourDamageToTargetCreatureThisTurnEffect
                        .to(Subjects.target(), RedirectedDamage.YOU)
                        .combatDamageOnly()
                        .targeting(new PermanentIsAttackingPredicate()));
    }
}
