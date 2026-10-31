package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "66")
public class OctopusForm extends Card {

    public OctopusForm() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET))
                .addSpellEffect(new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
