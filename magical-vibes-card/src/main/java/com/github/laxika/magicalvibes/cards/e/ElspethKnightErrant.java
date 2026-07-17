package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ElspethKnightErrantEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "9")
public class ElspethKnightErrant extends Card {

    public ElspethKnightErrant() {
        // +1: Create a 1/1 white Soldier creature token.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(CreateTokenEffect.whiteSoldier(1)),
                "+1: Create a 1/1 white Soldier creature token."
        ));

        // +1: Target creature gets +3/+3 and gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new BoostTargetCreatureEffect(3, 3), new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "+1: Target creature gets +3/+3 and gains flying until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")
        ));

        // −8: You get an emblem with "Artifacts, creatures, enchantments, and lands you control have indestructible."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new ElspethKnightErrantEmblemEffect()),
                "−8: You get an emblem with \"Artifacts, creatures, enchantments, and lands you control have indestructible.\""
        ));
    }
}
