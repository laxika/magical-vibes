package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerIsActiveOpponentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "6")
@CardRegistration(set = "NCC", collectorNumber = "101")
public class TheBeamtownBullies extends Card {

    public TheBeamtownBullies() {
        CardAllOfPredicate nonlegendaryCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffect()),
                "{T}: Target opponent whose turn it is puts target nonlegendary creature card from your graveyard onto the battlefield under their control. It gains haste. Goad it. At the beginning of the next end step, exile it.",
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerIsActiveOpponentPredicate(),
                                "Target must be an opponent whose turn it is"),
                        new GraveyardCardPredicateTargetFilter(
                                nonlegendaryCreature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                2,
                2));
    }
}
