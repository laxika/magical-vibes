package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ARB", collectorNumber = "142")
public class TraceOfAbundance extends Card {

    public TraceOfAbundance() {
        // Enchant land.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        ))
                // Enchanted land has shroud.
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ENCHANTED_PERMANENT))
                // Whenever enchanted land is tapped for mana, its controller adds an additional one mana of any color.
                .addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect()));
    }
}
