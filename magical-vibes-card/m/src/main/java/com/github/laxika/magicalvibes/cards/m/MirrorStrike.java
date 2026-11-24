package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "17")
public class MirrorStrike extends Card {

    public MirrorStrike() {
        target(TargetFilters.unblockedAttackingCreature())
                .addSpellEffect(RedirectNextDamageFromChosenSourceToSelfEffect.toItsController(Subjects.target())
                        .dealtTo(Subjects.you())
                        .combatDamageOnly()
                        .allThisTurn());
    }
}
