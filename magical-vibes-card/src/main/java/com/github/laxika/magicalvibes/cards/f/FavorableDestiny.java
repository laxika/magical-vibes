package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "16")
public class FavorableDestiny extends Card {

    public FavorableDestiny() {
        target(TargetFilters.creature())
                // Enchanted creature gets +1/+2 as long as it's white.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))))
                // Enchanted creature has shroud as long as its controller controls another creature.
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ENCHANTED_CREATURE,
                        new PermanentControllerControlsPermanentPredicate(new PermanentIsCreaturePredicate(), true)));
    }
}
