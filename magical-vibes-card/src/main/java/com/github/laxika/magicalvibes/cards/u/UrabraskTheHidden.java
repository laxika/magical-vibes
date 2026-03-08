package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "98")
public class UrabraskTheHidden extends Card {

    public UrabraskTheHidden() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(Set.of(CardType.CREATURE), true));
    }
}
