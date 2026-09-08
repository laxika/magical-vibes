package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
                .addEffect(EffectSlot.STATIC,
                        new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.TOY, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new SetBasePowerToughnessEffect(0, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new CantBeTurnedFaceUpEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
