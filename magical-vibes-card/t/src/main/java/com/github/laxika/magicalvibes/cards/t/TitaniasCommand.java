package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "194")
public class TitaniasCommand extends Card {

    public TitaniasCommand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target player's graveyard. You gain 1 life for each card exiled this way",
                        List.of(
                                new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING),
                                new GainLifeEffect(new EventValue())
                        ),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for up to two land cards, put them onto the battlefield tapped, then shuffle",
                        new SearchLibraryEffect(new Fixed(2), new CardTypePredicate(CardType.LAND),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 2/2 green Bear creature tokens",
                        new CreateTokenEffect(2, "Bear", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.BEAR), Set.of(), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on each creature you control",
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 2, new PermanentIsCreaturePredicate()))
        ), 2));
    }
}
