package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "26")
public class HemlockVial extends Card {

    public HemlockVial() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(), new LoseLifeEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES,
                                new PermanentIsEquippedPredicate()),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_PERMANENTS,
                                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))
                ),
                "{B}, {T}, Sacrifice this artifact: Each equipped creature and Equipment you control gains deathtouch until end of turn."
        ));
    }
}
