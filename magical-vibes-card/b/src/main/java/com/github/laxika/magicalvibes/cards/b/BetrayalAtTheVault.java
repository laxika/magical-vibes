package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "155")
public class BetrayalAtTheVault extends Card {

    public BetrayalAtTheVault() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toAnyTargetInGroup(new SourcePower(), 1).dealtBy(Subjects.target(0)));
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toAnyTargetInGroup(new SourcePower(), 2).dealtBy(Subjects.target(0)));
    }
}
