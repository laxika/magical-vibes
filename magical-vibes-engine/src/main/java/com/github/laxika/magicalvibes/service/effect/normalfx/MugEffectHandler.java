package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MugEffect;
import com.github.laxika.magicalvibes.model.filter.CardIdSetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Resolves the Mug keyword action. */
@Component
@RequiredArgsConstructor
public class MugEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MugEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> milledCards = gameData.orderedPlayerIds.stream()
                .flatMap(playerId -> graveyardService.resolveMillPlayerIncludingExiled(gameData, playerId, 1).stream())
                .toList();

        if (milledCards.stream().anyMatch(card -> card.hasType(CardType.LAND))) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, controllerId, CreateTokenEffect.ofTreasureToken(1),
                    entry.getCard().getSetCode()));
        }

        List<Card> spells = milledCards.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        Set<UUID> graveyardSpellIds = spells.stream()
                .filter(card -> gameData.findExiledCard(card.getId()) == null)
                .map(Card::getId)
                .collect(Collectors.toUnmodifiableSet());
        List<UUID> exiledSpellIds = spells.stream()
                .filter(card -> gameData.findExiledCard(card.getId()) != null)
                .map(Card::getId)
                .toList();
        if (!graveyardSpellIds.isEmpty()) {
            gameData.graveyardCastFilterPermissionsThisTurn.add(
                    new GameData.GraveyardCastFilterPermission(
                            controllerId, new CardIdSetPredicate(graveyardSpellIds), true, null, null,
                            true, false, null, null, 0));
        }
        if (!exiledSpellIds.isEmpty()) {
            gameData.registerExilePlayPermissionGroup(UUID.randomUUID(), 1, exiledSpellIds);
            for (UUID cardId : exiledSpellIds) {
                gameData.exilePlayPermissions.put(cardId, controllerId);
                gameData.exilePlayPermissionsExpireEndOfTurn.add(cardId);
            }
        }
        if (!spells.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " may cast one spell among the cards milled this way this turn."));
        }
    }
}
