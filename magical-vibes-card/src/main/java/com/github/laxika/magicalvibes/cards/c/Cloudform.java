package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeAuraManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "32")
public class Cloudform extends Card {

    public Cloudform() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeAuraManifestTopCardEffect());
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.HEXPROOF), GrantScope.ENCHANTED_CREATURE));
    }
}
