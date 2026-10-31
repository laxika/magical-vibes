package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "99")
public class NecrogenCommunion extends Card {

    public NecrogenCommunion() {
        target(TargetFilters.creatureYouControl())
                .addStaticEffect(new GrantKeywordEffect(Keyword.TOXIC, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantEffectEffect(new ToxicEffect(2), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true));
    }
}
