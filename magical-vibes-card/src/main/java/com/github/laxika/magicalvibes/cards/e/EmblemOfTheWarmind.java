package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import static com.github.laxika.magicalvibes.model.filter.TargetFilters.creatureYouControl;

@CardRegistration(set = "FUT", collectorNumber = "112")
public class EmblemOfTheWarmind extends Card {

    public EmblemOfTheWarmind() {
        target(creatureYouControl());
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
    }
}
