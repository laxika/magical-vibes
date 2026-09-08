package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "14")
public class GempalmAvenger extends Card {

    public GempalmAvenger() {
        PermanentHasSubtypePredicate soldier = new PermanentHasSubtypePredicate(CardSubtype.SOLDIER);
        addEffect(EffectSlot.ON_SELF_CYCLED, SequenceEffect.of(
                new BoostAllCreaturesEffect(1, 1, soldier),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ALL_CREATURES, soldier)));
        addCycling("{2}{W}");
    }
}
