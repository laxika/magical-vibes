package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Reveal top card creature-to-battlefield or may-bottom — e.g. Lurking Predators.
 */
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureToBattlefieldOrMayBottomHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureToBattlefieldOrMayBottomEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealTopCardCreatureToBattlefieldOrMayBottomEffect effect =
                (RevealTopCardCreatureToBattlefieldOrMayBottomEffect) ability.effects().getFirst();
        var deck = gameData.playerDecks.get(player.getId());
        boolean matchingCard = !deck.isEmpty()
                && predicateEvaluationService.matchesCardPredicate(
                        deck.getFirst(), effect.predicate(), ability.sourceCard().getId());
        if (effect.mayPutMatching() && matchingCard) {
            mayMiscHandlerService.handleLookAtTopCardPutLandOrCreatureChoice(gameData, player, accepted);
        } else {
            mayMiscHandlerService.handleRevealTopCardMayBottomChoice(gameData, player, accepted);
        }
    }
}
