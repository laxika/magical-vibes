package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "HOC", collectorNumber = "116")
@CardRegistration(set = "HOC", collectorNumber = "201")
@CardRegistration(set = "LTC", collectorNumber = "33")
@CardRegistration(set = "LTC", collectorNumber = "116")
@CardRegistration(set = "LTC", collectorNumber = "435")
public class OrcishSiegemaster extends Card {

    public OrcishSiegemaster() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.TRAMPLE,
                GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ORC, CardSubtype.GOBLIN))));
        addTrigger(Triggers.attacks(),
                new BoostSelfEffect(new GreatestPowerAmongControlled(), new Fixed(0)));
    }
}
