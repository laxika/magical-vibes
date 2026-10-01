package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.HeistTargetOpponentLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "9")
public class GraveExpectations extends Card {

    public GraveExpectations() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Heist target opponent's library",
                        new HeistTargetOpponentLibraryEffect(),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile up to three target cards from your opponents' graveyards. You gain 3 life.",
                        List.of(
                                new ExileGraveyardCardsEffect(3, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD),
                                new GainLifeEffect(3)),
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        null, 0, 3, false, null)
        )));
    }
}
