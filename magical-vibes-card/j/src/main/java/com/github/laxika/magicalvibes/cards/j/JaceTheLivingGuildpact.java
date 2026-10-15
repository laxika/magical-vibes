package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "62")
public class JaceTheLivingGuildpact extends Card {

    public JaceTheLivingGuildpact() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LookAtTopCardsOfTargetLibraryEffect(2, TargetLibraryAction.PUT_ONE_INTO_GRAVEYARD)),
                "+1: Look at the top two cards of your library. Put one of them into your graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(ReturnToHandEffect.target()),
                "−3: Return another target nonland permanent to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        "Target must be another nonland permanent")
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new EachPlayerShufflesZonesIntoLibraryEffect(),
                        new DrawCardEffect(7)
                ),
                "−8: Each player shuffles their hand and graveyard into their library. You draw seven cards."
        ));
    }
}
