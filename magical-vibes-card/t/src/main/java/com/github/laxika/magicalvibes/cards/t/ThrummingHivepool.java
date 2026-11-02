package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "247")
public class ThrummingHivepool extends Card {

    public ThrummingHivepool() {
        addStaticEffect(new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SLIVER), CountScope.CONTROLLER)));
        addStaticEffect(new GrantKeywordEffect(
                Set.of(Keyword.DOUBLE_STRIKE, Keyword.HASTE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CreateTokenEffect(
                2,
                "Sliver",
                1,
                1,
                null,
                List.of(CardSubtype.SLIVER),
                Set.of(),
                Set.of()));
    }
}
