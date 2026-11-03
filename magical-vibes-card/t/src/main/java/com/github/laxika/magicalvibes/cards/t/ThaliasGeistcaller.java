package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "7")
public class ThaliasGeistcaller extends Card {

    public ThaliasGeistcaller() {
        // Whenever you cast a spell from your graveyard, create a 1/1 white Spirit creature token
        // with flying.
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.GRAVEYARD)),
                CreateTokenEffect.whiteSpirit(1));

        // Sacrifice a Spirit: This creature gains indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                                "Sacrifice a Spirit",
                                false),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)
                ),
                "Sacrifice a Spirit: This creature gains indestructible until end of turn."
        ));
    }
}
