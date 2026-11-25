package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "112")
public class DuelTactics extends Card {

    public DuelTactics() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(1))
                .addSpellEffect(new CantBlockThisTurnEffect(Subjects.target()));
        addCastingOption(new FlashbackCast("{1}{R}"));
    }
}
