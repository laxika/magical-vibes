package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "32")
public class RabarooTroop extends Card {

    public RabarooTroop() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                new GainLifeEffect(1));
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAINS))),
                "Plainscycling {2} ({2}, Discard this card: Search your library for a Plains card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
