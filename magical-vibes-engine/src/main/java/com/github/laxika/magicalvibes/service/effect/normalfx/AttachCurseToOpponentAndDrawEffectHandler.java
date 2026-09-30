package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachCurseToOpponentAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Lynde's choice of a Curse and its new opponent host. */
@Component
@RequiredArgsConstructor
public class AttachCurseToOpponentAndDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachCurseToOpponentAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> curses = attachedCurses(gameData, controllerId);
        List<UUID> opponents = opponentsOf(gameData, controllerId);
        if (curses.isEmpty() || opponents.isEmpty()) {
            return;
        }

        UUID fixedOpponentId = opponents.size() == 1 ? opponents.getFirst() : null;
        if (curses.size() == 1) {
            if (fixedOpponentId == null) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.LyndeOpponentChoice(controllerId, curses.getFirst().getId()));
                playerInputService.beginPlayerChoice(gameData, controllerId, opponents,
                        entry.getCard().getName() + " — choose an opponent.");
            } else {
                attachAndQueueDraw(gameData, entry, curses.getFirst().getId(), fixedOpponentId,
                        entry.getResolvingEffectIndex() + 1);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.LyndeCurseChoice(controllerId, fixedOpponentId));
        playerInputService.beginPermanentChoice(gameData, controllerId,
                curses.stream().map(Permanent::getId).toList(),
                gameData.interaction.permanentChoiceContext(),
                entry.getCard().getName() + " — choose a Curse attached to you.");
    }

    public void completeCurseChoice(GameData gameData, UUID curseId,
                                    PermanentChoiceContext.LyndeCurseChoice context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }

        List<Permanent> curses = attachedCurses(gameData, context.controllerId());
        if (curses.stream().noneMatch(permanent -> permanent.getId().equals(curseId))) {
            return;
        }

        List<UUID> opponents = opponentsOf(gameData, context.controllerId());
        if (opponents.isEmpty()) {
            return;
        }
        if (context.fixedOpponentId() != null) {
            attachAndQueueDraw(gameData, entry, curseId, context.fixedOpponentId(),
                    gameData.pendingEffectResolutionIndex);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.LyndeOpponentChoice(context.controllerId(), curseId));
        playerInputService.beginPlayerChoice(gameData, context.controllerId(), opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId,
                                       PermanentChoiceContext.LyndeOpponentChoice context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null || !opponentsOf(gameData, context.controllerId()).contains(opponentId)) {
            return;
        }
        attachAndQueueDraw(gameData, entry, context.curseId(), opponentId,
                gameData.pendingEffectResolutionIndex);
    }

    private void attachAndQueueDraw(GameData gameData, StackEntry entry, UUID curseId,
                                    UUID opponentId, int drawInsertionIndex) {
        Permanent curse = gameQueryService.findPermanentById(gameData, curseId);
        if (curse == null || !isCurseAttachedTo(curse, entry.getControllerId())
                || !gameData.playerIds.contains(opponentId)) {
            return;
        }

        UUID curseControllerId = gameQueryService.findPermanentController(gameData, curseId);
        if (curseControllerId == null
                || !auraAttachmentService.canEnchantPlayer(gameData, curse.getCard(), curseControllerId, opponentId)) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(curse.getId());
        curse.setAttachedTo(opponentId);
        curse.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.text(curse.getCard().getName() + " is now attached to "
                + gameData.playerIdToName.get(opponentId) + "."));
        entry.insertEffectsToResolve(drawInsertionIndex, List.of(new DrawCardEffect(2)));
    }

    private List<Permanent> attachedCurses(GameData gameData, UUID playerId) {
        List<Permanent> result = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (isCurseAttachedTo(permanent, playerId)) {
                    result.add(permanent);
                }
            }
        }
        return result;
    }

    private boolean isCurseAttachedTo(Permanent permanent, UUID playerId) {
        return permanent.getAttachedTo() != null
                && permanent.getAttachedTo().equals(playerId)
                && permanent.getCard().isAura()
                && permanent.getCard().getSubtypes().contains(CardSubtype.CURSE);
    }

    private List<UUID> opponentsOf(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
