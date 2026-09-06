package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "217")
public class RiveteersCharm extends Card {

    public RiveteersCharm() {
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");
        var playerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var greatestCreatureOrPlaneswalker = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                new PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate()));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent sacrifices a creature or planeswalker they control with the greatest mana value among creatures and planeswalkers they control",
                        new SacrificePermanentsEffect(1, greatestCreatureOrPlaneswalker,
                                SacrificeRecipient.TARGET_PLAYER),
                        opponentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top three cards of your library. Until your next end step, you may play those cards",
                        new ExileTopCardsMayPlayUntilNextEndStepEffect(3)),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target player's graveyard",
                        new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE),
                        playerFilter)
        )));
    }
}
