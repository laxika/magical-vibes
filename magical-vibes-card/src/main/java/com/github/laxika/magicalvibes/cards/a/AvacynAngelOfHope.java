package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "AVR", collectorNumber = "6")
@CardRegistration(set = "INR", collectorNumber = "477")
public class AvacynAngelOfHope extends Card {

    public AvacynAngelOfHope() {
        // Other permanents you control have indestructible. The source permanent is excluded from
        // static bonus computation, so OWN_PERMANENTS models the "other" wording; Avacyn's own
        // indestructible (plus flying and vigilance) is auto-loaded from Scryfall.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS));
    }
}
