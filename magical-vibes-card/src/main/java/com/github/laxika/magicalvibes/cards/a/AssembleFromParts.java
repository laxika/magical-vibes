package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantGraveyardAbilityToTargetCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSourceFromExileIntoOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "25")
public class AssembleFromParts extends Card {

    public AssembleFromParts() {
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        addEffect(EffectSlot.SPELL, new PerpetuallyGrantGraveyardAbilityToTargetCardEffect(
                new ActivatedAbility(
                        false,
                        "{1}{B}{B}",
                        List.of(
                                new ExileSelfFromGraveyardCost(),
                                new CreateTokenCopyOfSourceEffect(
                                        false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4),
                                new ShuffleSourceFromExileIntoOwnersLibraryEffect()),
                        "{1}{B}{B}, Exile this card from your graveyard: Create a token that's a copy of it, "
                                + "except it's a 4/4 black Zombie in addition to its other types. Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED)));
    }
}
