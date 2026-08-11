package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "17")
public class GallantFowlknight extends Card {

    public GallantFowlknight() {
        var kithkin = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.KITHKIN));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(1, 0));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES, kithkin));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE), GrantScope.SELF, null,
                        GrantDuration.END_OF_TURN, kithkin));
    }
}
