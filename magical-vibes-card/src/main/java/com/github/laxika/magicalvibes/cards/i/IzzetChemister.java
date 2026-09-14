package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "A25", collectorNumber = "138")
public class IzzetChemister extends Card {

    public IzzetChemister() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new ExileTargetCardFromGraveyardAndTrackWithSourceEffect(
                        instantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                "{R}, {T}: Exile target instant or sorcery card from your graveyard.",
                new GraveyardCardPredicateTargetFilter(
                        instantOrSorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(
                                instantOrSorcery, true)),
                "{1}{R}, {T}, Sacrifice Izzet Chemister: Cast any number of cards exiled with this "
                        + "creature without paying their mana costs."));
    }
}
