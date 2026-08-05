package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

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
                TargetFilters.creature()
        ));

        // −8: You get an emblem with "Artifacts, creatures, enchantments, and lands you control have indestructible."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsEnchantmentPredicate(),
                                        new PermanentIsLandPredicate())))),
                        "Artifacts, creatures, enchantments, and lands you control have indestructible.")),
                "−8: You get an emblem with \"Artifacts, creatures, enchantments, and lands you control have indestructible.\""
        ));
    }
}
