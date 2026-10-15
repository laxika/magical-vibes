package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "107")
@CardRegistration(set = "TMP", collectorNumber = "87")
@CardRegistration(set = "INV", collectorNumber = "72")
public class ShimmeringWings extends Card {

    public ShimmeringWings() {
        // Enchant creature
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(ReturnToHandEffect.self()), "{U}: Return Shimmering Wings to its owner's hand."));
    }
}
