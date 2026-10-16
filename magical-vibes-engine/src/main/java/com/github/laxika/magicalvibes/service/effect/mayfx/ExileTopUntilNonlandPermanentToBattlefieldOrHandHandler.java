package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.Card;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandPermanentToBattlefieldOrHandHandler implements MayEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final AuraAttachmentService auraAttachmentService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ExiledCardEntry exiled = ability.targetCardId() == null
                ? null
                : gameData.findExiledCard(ability.targetCardId());
        if (exiled != null && accepted && exiled.card().isAura()) {
            Card aura = exiled.card();
            UUID controllerId = ability.controllerId();
            List<UUID> permanentIds = gameData.orderedPlayerIds.stream()
                    .flatMap(playerId -> gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream())
                    .filter(candidate -> auraAttachmentService.canEnchant(gameData, aura, controllerId, candidate))
                    .map(Permanent::getId).toList();
            List<UUID> playerIds = aura.isEnchantPlayer() ? gameData.orderedPlayerIds.stream()
                    .filter(playerId -> auraAttachmentService.canEnchantPlayer(gameData, aura, controllerId, playerId))
                    .toList() : List.of();
            if (permanentIds.isEmpty() && playerIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.textCardText("", aura,
                        " stays exiled because it cannot legally enchant anything."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            gameData.removeFromExile(aura.getId());
            gameData.interaction.setPendingAuraCard(aura);
            gameData.interaction.setPendingAuraOwnerId(controllerId);
            playerInputService.beginAnyTargetChoice(gameData, controllerId, permanentIds, playerIds,
                    "Choose what " + aura.getName() + " enchants as it enters.");
            return;
        }
        if (exiled != null && gameData.removeFromExile(exiled.card().getId())) {
            if (accepted) {
                graveyardReturnSupport.putCardOntoBattlefieldFromExile(
                        gameData, ability.controllerId(), exiled.card());
            } else {
                gameData.addCardToHand(ability.controllerId(), exiled.card());
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " puts ", exiled.card(), " into their hand."));
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
