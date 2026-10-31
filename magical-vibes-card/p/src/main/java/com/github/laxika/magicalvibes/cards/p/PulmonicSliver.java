package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "36")
@CardRegistration(set = "SLD", collectorNumber = "613")
@CardRegistration(set = "TSR", collectorNumber = "34")
public class PulmonicSliver extends Card {

    public PulmonicSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addStaticEffect(new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.ALL_CREATURES_INCLUDING_SELF, sliver));
        addStaticEffect(new GrantEffectEffect(
                new PutOnTopOfLibraryInsteadOfDyingEffect(true), GrantScope.ALL_PERMANENTS, sliver));
        addStaticEffect(new GrantEffectEffect(
                new PutOnTopOfLibraryInsteadOfDyingEffect(true), GrantScope.SELF, sliver));
    }
}
