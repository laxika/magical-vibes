package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "263")
public class RealmbreakerTheInvasionTree extends Card {

    public RealmbreakerTheInvasionTree() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new MillEffect(3, MillRecipient.TARGET_PLAYER),
                        new TargetPlayerChoosesCardFromGraveyardToBattlefieldEffect(
                                new CardTypePredicate(CardType.LAND), true, true)
                ),
                "{2}, {T}: Target opponent mills three cards. Put a land card from their graveyard onto the battlefield tapped under your control. It gains \"If this land would leave the battlefield, exile it instead of putting it anywhere else.\"",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{10}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardsInLibrary(CountScope.CONTROLLER),
                                new CardSubtypePredicate(CardSubtype.PRAETOR),
                                LibrarySearchDestination.BATTLEFIELD)
                ),
                "{10}, {T}, Sacrifice Realmbreaker, the Invasion Tree: Search your library for any number of Praetor cards, put them onto the battlefield, then shuffle."
        ));
    }
}
