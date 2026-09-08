package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "77")
public class GaeasTouch extends Card {

    public GaeasTouch() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        CardPredicateUtils.basicLand(),
                                        new CardSubtypePredicate(CardSubtype.FOREST))),
                                "basic Forest"),
                        "Put a basic Forest card from your hand onto the battlefield?")),
                "{0}: You may put a basic Forest card from your hand onto the battlefield. "
                        + "Activate only as a sorcery and only once each turn.",
                null,
                null,
                1,
                ActivationTimingRestriction.SORCERY_SPEED));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.GREEN, 2)),
                "Sacrifice this enchantment: Add {G}{G}."));
    }
}
