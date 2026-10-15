package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealPowerDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndDealDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetCreatureThenCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ROE", collectorNumber = "214")
public class SarkhanTheMad extends Card {

    public SarkhanTheMad() {
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new RevealTopCardPutIntoHandAndDealDamageToSelfEffect()),
                "0: Reveal the top card of your library and put it into your hand. Sarkhan deals damage to himself equal to that card's mana value."));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new SacrificeTargetCreatureThenCreateTokenEffect(
                        new CreateTokenEffect("Dragon", 5, 5, CardColor.RED, List.of(CardSubtype.DRAGON),
                                Set.of(Keyword.FLYING), Set.of()))),
                "-2: Target creature's controller sacrifices it, then that player creates a 5/5 red Dragon creature token with flying.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new ControlledCreaturesDealPowerDamageToTargetPlayerOrPlaneswalkerEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON))))),
                "-4: Each Dragon creature you control deals damage equal to its power to target player or planeswalker.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player or planeswalker")));
    }
}
