package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentSharingTypeWithEnteringPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Cloudstone Curio's resolution-time permanent choice. */
@Component
@RequiredArgsConstructor
public class ReturnPermanentSharingTypeWithEnteringPermanentToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnPermanentSharingTypeWithEnteringPermanentToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID enteringPermanentId = entry.getTriggeringPermanentId();
        Set<CardType> enteringTypes = enteringPermanentTypes(gameData, entry, enteringPermanentId);
        List<UUID> choices = new ArrayList<>();

        if (!enteringTypes.isEmpty()) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (!permanent.getId().equals(enteringPermanentId)
                            && sharesPermanentType(gameData, permanent, enteringTypes)) {
                        choices.add(permanent.getId());
                    }
                }
            }
        }

        if (choices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData,
                    GameLog.text(playerName + " controls no other permanent sharing a permanent type with it."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, choices,
                "Choose another permanent you control to return to its owner's hand.");
    }

    private Set<CardType> enteringPermanentTypes(GameData gameData, StackEntry entry,
                                                  UUID enteringPermanentId) {
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (enteringPermanent != null) {
            return effectivePermanentTypes(gameData, enteringPermanent);
        }

        Card enteringCard = findCardById(gameData, entry.getTriggeringCardId());
        if (enteringCard == null) {
            return Set.of();
        }
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        if (enteringCard.getType() != null && enteringCard.getType().isPermanentType()) {
            types.add(enteringCard.getType());
        }
        enteringCard.getAdditionalTypes().stream()
                .filter(CardType::isPermanentType)
                .forEach(types::add);
        return types;
    }

    private Set<CardType> effectivePermanentTypes(GameData gameData, Permanent permanent) {
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        if (gameQueryService.isLand(gameData, permanent)) types.add(CardType.LAND);
        if (gameQueryService.isCreature(gameData, permanent)) types.add(CardType.CREATURE);
        if (gameQueryService.isEnchantment(gameData, permanent)) types.add(CardType.ENCHANTMENT);
        if (gameQueryService.isArtifact(gameData, permanent)) types.add(CardType.ARTIFACT);
        if (gameQueryService.isPlaneswalker(gameData, permanent)) types.add(CardType.PLANESWALKER);
        if (gameQueryService.isBattle(gameData, permanent)) types.add(CardType.BATTLE);
        if (gameQueryService.isKindred(gameData, permanent)) types.add(CardType.KINDRED);
        return types;
    }

    private boolean sharesPermanentType(GameData gameData, Permanent permanent,
                                        Set<CardType> enteringTypes) {
        return effectivePermanentTypes(gameData, permanent).stream().anyMatch(enteringTypes::contains);
    }

    private Card findCardById(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())) {
                    return permanent.getCard();
                }
            }
        }
        for (List<Card> cards : gameData.playerHands.values()) {
            for (Card card : cards) {
                if (cardId.equals(card.getId())) {
                    return card;
                }
            }
        }
        for (List<Card> cards : gameData.playerGraveyards.values()) {
            for (Card card : cards) {
                if (cardId.equals(card.getId())) {
                    return card;
                }
            }
        }
        for (List<Card> cards : gameData.playerDecks.values()) {
            for (Card card : cards) {
                if (cardId.equals(card.getId())) {
                    return card;
                }
            }
        }
        for (List<Card> cards : gameData.playerCommandZones.values()) {
            for (Card card : cards) {
                if (cardId.equals(card.getId())) {
                    return card;
                }
            }
        }
        for (var exiledCard : gameData.exiledCards) {
            if (cardId.equals(exiledCard.card().getId())) {
                return exiledCard.card();
            }
        }
        return null;
    }
}
