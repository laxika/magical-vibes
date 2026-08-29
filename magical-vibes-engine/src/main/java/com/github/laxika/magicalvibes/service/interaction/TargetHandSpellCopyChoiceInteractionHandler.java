package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetHandSpellCopyChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TargetHandSpellCopyChoice> {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.TargetHandSpellCopyChoice> handledType() {
        return PendingInteraction.TargetHandSpellCopyChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TargetHandSpellCopyChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen.size() > 1 || chosen.stream().distinct().count() != chosen.size()) {
            throw new IllegalStateException("Choose at most one card");
        }
        if (chosen.stream().anyMatch(cardId -> !interaction.validCardIds().contains(cardId))) {
            throw new IllegalStateException("Chosen card was not in the revealed hand");
        }
        gameData.interaction.clearAwaitingInput();

        if (chosen.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID chosenId = chosen.getFirst();
        Card source = gameData.playerHands.getOrDefault(interaction.targetPlayerId(), List.of())
                .stream()
                .filter(card -> card.getId().equals(chosenId))
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .findFirst()
                .orElse(null);
        if (source == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card copy = copySupport.createCopyCard(source);
        exileService.exileCard(gameData, player.getId(), copy);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                player.getId(),
                List.of(new MayCastCopyWithoutPayingManaCostEffect()),
                "Cast the copy of " + copy.getName() + " without paying its mana cost?",
                copy.getId()));
        gameLogService.append(gameData,
                GameLog.textCardText(player.getUsername() + " creates a copy of ", source, "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
