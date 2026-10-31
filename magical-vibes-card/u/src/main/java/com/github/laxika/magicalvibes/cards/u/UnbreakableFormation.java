package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RNA", collectorNumber = "29")
@CardRegistration(set = "SLD", collectorNumber = "310")
@CardRegistration(set = "RVR", collectorNumber = "32")
@CardRegistration(set = "MOC", collectorNumber = "213")
@CardRegistration(set = "LTC", collectorNumber = "179")
@CardRegistration(set = "DMC", collectorNumber = "106")
@CardRegistration(set = "MIC", collectorNumber = "95")
@CardRegistration(set = "BRC", collectorNumber = "78")
public class UnbreakableFormation extends Card {

    public UnbreakableFormation() {
        addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES));
        addSpellEffect(new ConditionalEffect(new CastDuringMainPhase(),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())));
        addSpellEffect(new ConditionalEffect(new CastDuringMainPhase(),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES)));
    }
}
