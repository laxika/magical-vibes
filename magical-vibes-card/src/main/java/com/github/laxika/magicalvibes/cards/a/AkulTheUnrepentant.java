package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "189")
public class AkulTheUnrepentant extends Card {

    public AkulTheUnrepentant() {
        var otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(otherCreature, otherCreature, otherCreature),
                                List.of("another creature", "another creature", "another creature")
                        ),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                                "Put a creature card from your hand onto the battlefield?"
                        )
                ),
                "Sacrifice three other creatures: You may put a creature card from your hand onto the battlefield. "
                        + "Activate only as a sorcery and only once each turn.",
                null,
                null,
                1,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
