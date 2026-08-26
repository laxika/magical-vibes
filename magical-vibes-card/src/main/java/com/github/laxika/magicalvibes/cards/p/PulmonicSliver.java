package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "36")
public class PulmonicSliver extends Card {

    public PulmonicSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.ALL_CREATURES_INCLUDING_SELF, sliver));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new PutOnTopOfLibraryInsteadOfDyingEffect(true), GrantScope.ALL_PERMANENTS, sliver));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new PutOnTopOfLibraryInsteadOfDyingEffect(true), GrantScope.SELF, sliver));
    }
}
