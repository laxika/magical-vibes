package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "DOM", collectorNumber = "77")
public class BlessingOfBelzenlok extends Card {

    public BlessingOfBelzenlok() {
        addSpellEffect(new BoostTargetCreatureEffect(2, 1));
        addSpellEffect(GrantKeywordEffect.toTargetIf(
                Keyword.LIFELINK, new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
    }
}
