package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M12", collectorNumber = "100")
public class HideousVisage extends Card {

    public HideousVisage() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.OWN_CREATURES));
    }
}
