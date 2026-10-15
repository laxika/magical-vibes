package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "IKO", collectorNumber = "230")
public class SonorousHowlbonder extends Card {

    public SonorousHowlbonder() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantBeBlockedByFewerThanNCreaturesEffect(3),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.MENACE)));
    }
}
