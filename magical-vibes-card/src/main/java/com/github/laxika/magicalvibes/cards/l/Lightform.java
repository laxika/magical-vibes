package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeAuraManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "16")
public class Lightform extends Card {

    public Lightform() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeAuraManifestTopCardEffect());
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.LIFELINK), GrantScope.ENCHANTED_CREATURE));
    }
}
