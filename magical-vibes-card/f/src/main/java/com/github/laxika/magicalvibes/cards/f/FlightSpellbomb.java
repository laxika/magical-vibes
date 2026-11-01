package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "155")
public class FlightSpellbomb extends Card {

    public FlightSpellbomb() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{T}, Sacrifice Flight Spellbomb: Target creature gains flying until end of turn.",
                TargetFilters.creature()
        ));

        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayPayManaEffect("{U}", new DrawCardEffect(1), "Pay {U} to draw a card?"));
    }
}
