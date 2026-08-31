package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "78")
public class CrownOfAscension extends Card {

    public CrownOfAscension() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect(Keyword.FLYING)
                ),
                "Sacrifice this Aura: Enchanted creature and other creatures that share a creature type with it gain flying until end of turn."
        ));
    }
}
