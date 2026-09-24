package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1836")
@CardRegistration(set = "C13", collectorNumber = "9")
@CardRegistration(set = "CMM", collectorNumber = "21")
@CardRegistration(set = "CMM", collectorNumber = "623")
public class DarksteelMutation extends Card {

    public DarksteelMutation() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new SetCardTypesEffect(Set.of(CardType.ARTIFACT, CardType.CREATURE),
                                GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.INSECT, GrantScope.ENCHANTED_CREATURE, true))
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new SetBasePowerToughnessEffect(0, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
