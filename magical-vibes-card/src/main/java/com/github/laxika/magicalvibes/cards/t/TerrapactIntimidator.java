package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForSourceControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "164")
public class TerrapactIntimidator extends Card {

    private static final CreateTokenEffect LANDER = CreateTokenEffect.ofArtifactToken(
            2,
            "Lander",
            List.of(CardSubtype.LANDER),
            List.of(new ActivatedAbility(
                    true,
                    "{2}",
                    List.of(
                            new SacrificeSelfCost(),
                            new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                                    LibrarySearchDestination.BATTLEFIELD_TAPPED)
                    ),
                    "{2}, {T}, Sacrifice this token: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle."
            ))
    );

    public TerrapactIntimidator() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new CreateTokenForSourceControllerEffect(LANDER),
                "Have this creature's controller create two Lander tokens?",
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                MayChoicePlayer.TARGET_PLAYER
        ));
    }
}
