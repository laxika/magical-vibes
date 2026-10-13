package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves an accepted demonstrate trigger (CR 702.144a): copies the spell for its controller and
 * for a chosen opponent, each of whom may choose new targets for their copy.
 */
@Component
@RequiredArgsConstructor
public class DemonstrateEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DemonstrateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        DemonstrateEffect demonstrate = (DemonstrateEffect) effect;
        StackEntry spell = demonstrate.spellSnapshot() != null
                ? demonstrate.spellSnapshot() : gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .findFirst()
                .orElse(null);
        if (spell == null || spell.isCopy()) {
            return;
        }

        Card spellCard = spell.getCard();
        if (spellCard.isCantBeCopied()) {
            return;
        }

        StackEntry spellSnapshot = new StackEntry(spell);
        UUID controllerId = entry.getControllerId();
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        if (opponents.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.DemonstrateOpponentChoice(
                    spellSnapshot, controllerId));
            playerInputService.beginPlayerChoice(gameData, controllerId, opponents,
                    spellCard.getName() + " — choose an opponent to copy it.");
            return;
        }
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                copySteps(spellSnapshot, controllerId, opponents.isEmpty() ? null : opponents.getFirst()));
    }

    public void completeOpponentChoice(GameData gameData, UUID chosenOpponentId,
                                       PermanentChoiceContext.DemonstrateOpponentChoice context) {
        if (!gameData.playerIds.contains(chosenOpponentId)
                || chosenOpponentId.equals(context.controllerId())
                || gameData.pendingEffectResolutionEntry == null) {
            return;
        }
        gameData.pendingEffectResolutionEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                copySteps(context.spellSnapshot(), context.controllerId(), chosenOpponentId));
    }

    /**
     * The controller's copy, then the opponent's. Each runs as its own resolution step so the
     * controller settles their copy's targets (CR 707.10c) before the opponent's copy exists.
     */
    private List<CardEffect> copySteps(StackEntry spellSnapshot, UUID controllerId, UUID opponentId) {
        List<CardEffect> steps = new ArrayList<>();
        steps.add(new CopyControllerCastSpellEffect(spellSnapshot, controllerId));
        if (opponentId != null) {
            steps.add(new CopyControllerCastSpellEffect(spellSnapshot, opponentId));
        }
        return steps;
    }
}
