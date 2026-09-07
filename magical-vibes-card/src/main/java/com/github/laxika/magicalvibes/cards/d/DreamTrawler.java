package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "214")
public class DreamTrawler extends Card {

    public DreamTrawler() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new BoostSelfEffect(1, 0));
        addEffect(EffectSlot.ON_ATTACK, new DrawCardEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF),
                        new TapPermanentsEffect(TapUntapScope.SELF)
                ),
                "Discard a card: This creature gains hexproof until end of turn. Tap it."
        ));
    }
}
