package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "127")
@CardRegistration(set = "SPM", collectorNumber = "271")
public class CosmicSpiderMan extends Card {

    public CosmicSpiderMan() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new GrantKeywordEffect(
                        Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE,
                                Keyword.LIFELINK, Keyword.HASTE),
                        GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.SPIDER)));
    }
}
