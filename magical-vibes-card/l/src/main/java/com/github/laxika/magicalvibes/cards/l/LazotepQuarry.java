package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "79")
@CardRegistration(set = "M3C", collectorNumber = "131")
public class LazotepQuarry extends Card {

    public LazotepQuarry() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}, Sacrifice a creature: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeCreatureCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice a creature: Add one mana of any color."
        ));

        // {X}{2}, {T}, Sacrifice a Desert: Exile target creature card with mana value X from your graveyard.
        // Create a token that's a copy of it, except it's a 4/4 black Zombie. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{2}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.DESERT),
                                "Sacrifice a Desert",
                                false),
                        new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardManaValueEqualsXPredicate())),
                                true,
                                List.of(CardSubtype.ZOMBIE),
                                false,
                                false,
                                CardColor.BLACK,
                                4,
                                4)
                ),
                "{X}{2}, {T}, Sacrifice a Desert: Exile target creature card with mana value X from your graveyard. "
                        + "Create a token that's a copy of it, except it's a 4/4 black Zombie. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
