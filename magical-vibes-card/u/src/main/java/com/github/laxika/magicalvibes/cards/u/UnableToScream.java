package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantBeTurnedFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "78")
public class UnableToScream extends Card {

    public UnableToScream() {
        target(TargetFilters.creature())
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantCardTypeEffect(CardType.CREATURE, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.TOY, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new SetBasePowerToughnessEffect(0, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new CantBeTurnedFaceUpEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
