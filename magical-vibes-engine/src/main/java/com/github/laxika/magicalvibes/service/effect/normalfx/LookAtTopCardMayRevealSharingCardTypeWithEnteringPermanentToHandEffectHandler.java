package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect.Stage;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Amareth's top-card check after a permanent enters under its controller's control. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect typedEffect =
                (LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect) effect;
        if (typedEffect.stage() != Stage.LOOK) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + entry.getCard().getName() + ")."));
            return;
        }

        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library ("
                        + entry.getCard().getName() + ")."));

        Set<CardType> enteringTypes = enteringPermanentTypes(gameData, entry);
        Card topCard = deck.getFirst();
        if (!sharesCardType(topCard, enteringTypes)) {
            log.info("Game {} - top card {} shares no card type with the entering permanent",
                    gameData.id, topCard.getName());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(typedEffect.withStage(Stage.MAY_HAND)),
                entry.getCard().getName() + " — Reveal " + topCard.getName()
                        + " and put it into your hand?"));
        log.info("Game {} - {} may reveal {} and put it into hand",
                gameData.id, playerName, topCard.getName());
    }

    private Set<CardType> enteringPermanentTypes(GameData gameData, StackEntry entry) {
        Permanent enteringPermanent = gameQueryService.findPermanentById(
                gameData, entry.getTriggeringPermanentId());
        if (enteringPermanent != null) {
            EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
            if (gameQueryService.isLand(gameData, enteringPermanent)) types.add(CardType.LAND);
            if (gameQueryService.isCreature(gameData, enteringPermanent)) types.add(CardType.CREATURE);
            if (gameQueryService.isEnchantment(gameData, enteringPermanent)) types.add(CardType.ENCHANTMENT);
            if (gameQueryService.isArtifact(gameData, enteringPermanent)) types.add(CardType.ARTIFACT);
            if (gameQueryService.isPlaneswalker(gameData, enteringPermanent)) types.add(CardType.PLANESWALKER);
            if (gameQueryService.isBattle(gameData, enteringPermanent)) types.add(CardType.BATTLE);
            if (gameQueryService.isKindred(gameData, enteringPermanent)) types.add(CardType.KINDRED);
            return types;
        }

        Card enteringCard = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (enteringCard == null) {
            return Set.of();
        }
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        if (enteringCard.getType() != null) {
            types.add(enteringCard.getType());
        }
        types.addAll(enteringCard.getAdditionalTypes());
        return types;
    }

    private boolean sharesCardType(Card card, Set<CardType> types) {
        return types.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(types::contains);
    }
}
