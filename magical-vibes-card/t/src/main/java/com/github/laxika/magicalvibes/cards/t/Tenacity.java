package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOI", collectorNumber = "42")
public class Tenacity extends Card {

    public Tenacity() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(1, 1));
        addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES));
        addSpellEffect(new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                new PermanentIsCreaturePredicate()));
    }
}
