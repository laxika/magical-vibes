package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "208")
@CardRegistration(set = "FDN", collectorNumber = "234")
public class VivienReid extends Card {

    public VivienReid() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        4,
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.LAND))))),
                "+1: Look at the top four cards of your library. You may reveal a creature or land card "
                        + "from among them and put it into your hand. Put the rest on the bottom of your "
                        + "library in a random order."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect()),
                "\u22123: Destroy target artifact, enchantment, or creature with flying.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate(),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasKeywordPredicate(Keyword.FLYING))))),
                        "Target must be an artifact, enchantment, or creature with flying")
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new StaticBoostEffect(
                                2,
                                2,
                                Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE),
                                GrantScope.OWN_CREATURES)),
                        "Creatures you control get +2/+2 and have vigilance, trample, and indestructible.")),
                "\u22128: You get an emblem with \"Creatures you control get +2/+2 and have vigilance, "
                        + "trample, and indestructible.\""
        ));
    }
}
