package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "3")
@CardRegistration(set = "SOC", collectorNumber = "134")
@CardRegistration(set = "WOC", collectorNumber = "60")
@CardRegistration(set = "FDC", collectorNumber = "14")
public class AngelicDestiny extends Card {

    public AngelicDestiny() {
        target(TargetFilters.creature())
                // Enchanted creature gets +4/+4 and has flying and first strike.
                .addStaticEffect(new StaticBoostEffect(
                        4, 4, Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE), GrantScope.ENCHANTED_CREATURE))
                // ... and is an Angel in addition to its other types.
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.ANGEL, GrantScope.ENCHANTED_CREATURE))
                // When enchanted creature dies, return this card to its owner's hand.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
