package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicatesOfTargetPermanentsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutChosenCardFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Resolves Snowborn Simulacra's targeted permanent duplicates and its X=5 choice. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicatesOfTargetPermanentsIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicatesOfTargetPermanentsIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> conjuredCards = new ArrayList<>();
        for (UUID targetId : entry.targetsForEffect(effect)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            Card copy = target.getCard().createCardCopy();
            copy.setOwnerId(entry.getControllerId());
            copy.freeze();
            gameData.perpetualAnyColorManaForCastCardIds.add(copy.getId());
            gameData.addCardToHand(entry.getControllerId(), copy);
            conjuredCards.add(copy);
            gameLogService.append(gameData, GameLog.cardThen(copy, " is conjured into "
                    + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
        }

        if (entry.getXValue() < 5 || conjuredCards.isEmpty()) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        int firstConjuredIndex = hand.size() - conjuredCards.size();
        List<Integer> validIndices = IntStream.range(firstConjuredIndex, hand.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                entry.getControllerId(), entry.getControllerId(), validIndices, 1,
                false, false, List.of(), entry.getSourcePermanentId(),
                "You may put one of the conjured cards onto the battlefield.",
                false, true, false, null, null, 0, null,
                false, false, false, false, false, false, 0, false,
                null, null, new PutChosenCardFromHandOntoBattlefieldEffect(), 0
        ).withKeepInHand());
    }
}
