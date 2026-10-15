package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "248")
public class GhostVacuum extends Card {

    public GhostVacuum() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new ExileTargetCardFromGraveyardAndTrackWithSourceEffect(
                        GraveyardSearchScope.ALL_GRAVEYARDS)),
                "{T}: Exile target card from a graveyard."));

        addActivatedAbility(new ActivatedAbility(
                true, "{6}",
                List.of(
                        new SacrificeSelfCost(),
                        new ReturnAllCardsExiledWithSourceEffect(
                                true,
                                new CardTypePredicate(CardType.CREATURE),
                                false,
                                Set.of(),
                                1,
                                1,
                                CardSubtype.SPIRIT,
                                CounterType.FLYING)),
                "{6}, {T}, Sacrifice this artifact: Put each creature card exiled with this artifact "
                        + "onto the battlefield under your control with a flying counter on it. Each of "
                        + "them is a 1/1 Spirit in addition to its other types. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
