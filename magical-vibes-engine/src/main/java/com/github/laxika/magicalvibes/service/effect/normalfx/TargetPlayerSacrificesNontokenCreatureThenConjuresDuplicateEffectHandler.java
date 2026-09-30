package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Grave Choice's targeted sacrifice and conditional duplicate. */
@Component
@RequiredArgsConstructor
public class TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, entry.getControllerId())) {
            return;
        }

        List<UUID> creatureIds = eligibleCreatureIds(gameData, targetPlayerId);
        if (creatureIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(targetPlayerId) + " has no nontoken creatures to sacrifice."));
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            sacrificeAndConjure(gameData, creature, targetPlayerId, entry,
                    (TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect) effect);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicate(
                        targetPlayerId, entry,
                        (TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect) effect));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                entry.getCard().getName() + " — Choose a nontoken creature to sacrifice.");
    }

    public void sacrificeAndConjure(GameData gameData, Permanent creature,
                                    UUID sacrificingPlayerId, StackEntry entry,
                                    TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect effect) {
        if (creature == null
                || !sacrificingPlayerId.equals(gameQueryService.findPermanentController(gameData, creature.getId()))
                || !gameQueryService.isCreature(gameData, creature)
                || creature.getCard().isToken()
                || gameQueryService.cantBeSacrificed(gameData, creature)
                || !gameQueryService.canEffectCauseSacrifice(gameData, sacrificingPlayerId, entry.getControllerId())) {
            return;
        }

        Card sacrificedCard = creature.getCard();
        boolean qualifiesForDuplicate = effect.maxManaValue() < 0
                || sacrificedCard.getManaValue() <= effect.maxManaValue();
        destructionSupport.sacrificeAndLog(gameData, creature, sacrificingPlayerId);

        if (!qualifiesForDuplicate) {
            return;
        }

        if (effect.mayDiscard()) {
            if (gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).isEmpty()) {
                return;
            }
            MayEffect mayDiscard = new MayEffect(
                    new DiscardCardThenEffect(null,
                            new ConjureDuplicateOfCardIntoHandEffect(sacrificedCard), "a card"),
                    "discard a card to conjure a duplicate of the sacrificed creature?");
            gameData.queueMayAbility(entry.getCard(), entry.getControllerId(), mayDiscard);
            return;
        }

        conjure(gameData, entry.getControllerId(), sacrificedCard);
    }

    private void conjure(GameData gameData, UUID recipientId, Card sourceCard) {
        Card duplicate = sourceCard.createCardCopy();
        duplicate.setOwnerId(recipientId);
        duplicate.freeze();
        gameData.perpetualAnyColorManaForCastCardIds.add(duplicate.getId());
        gameData.addCardToHand(recipientId, duplicate);
        gameLogService.append(gameData, GameLog.cardThen(duplicate, " is conjured into "
                + gameData.playerIdToName.get(recipientId) + "'s hand."));
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, UUID playerId) {
        List<UUID> creatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return creatureIds;
        }
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && !permanent.getCard().isToken()
                    && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                creatureIds.add(permanent.getId());
            }
        }
        return creatureIds;
    }
}
