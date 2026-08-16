package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPlayerPermanentsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "66")
public class TeferiTemporalPilgrim extends Card {

    public TeferiTemporalPilgrim() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new PutCountersOnSelfEffect(CounterType.LOYALTY));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DrawCardEffect()),
                "0: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Spirit", 2, 2, CardColor.BLUE,
                        List.of(CardSubtype.SPIRIT), Set.of(Keyword.VIGILANCE), Set.of(),
                        Map.of(EffectSlot.ON_CONTROLLER_DRAWS,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)))),
                "\u22122: Create a 2/2 blue Spirit creature token with vigilance and \"Whenever you draw a card, put a +1/+1 counter on this token.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                -12,
                List.of(
                        new ReturnPermanentControlledByPlayerToHandEffect(new PermanentTruePredicate(), "permanent"),
                        new ShuffleTargetPlayerPermanentsIntoLibraryEffect(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()))
                ),
                "\u221212: Target opponent chooses a permanent they control and returns it to its owner's hand. "
                        + "Then they shuffle each nonland permanent they control into its owner's library.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
