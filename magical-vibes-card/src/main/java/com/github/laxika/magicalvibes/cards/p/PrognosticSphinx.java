package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "60")
public class PrognosticSphinx extends Card {

    public PrognosticSphinx() {
        addEffect(EffectSlot.ON_ATTACK, new ScryEffect(3));

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
