package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DanceWithCalamityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.mayfx.MayEffectHandlerBean;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Dance with Calamity and its optional top-card exile loop. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DanceWithCalamityEffectHandler
        implements NormalEffectHandlerBean, MayEffectHandlerBean {

    private static final int MAX_TOTAL_MANA_VALUE = 13;

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DanceWithCalamityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " shuffles their library (" + entry.getCard().getName() + ")."));

        if (!beginExileChoice(gameData, entry.getCard(), controllerId, List.of(), 0)) {
            finish(gameData, entry.getCard(), controllerId, List.of(), 0);
        }
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DanceWithCalamityEffect repeat = ability.effects().stream()
                .filter(DanceWithCalamityEffect.class::isInstance)
                .map(DanceWithCalamityEffect.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Missing Dance with Calamity repeat state"));

        UUID controllerId = ability.controllerId();
        if (accepted) {
            List<Card> library = gameData.playerDecks.get(controllerId);
            if (library != null && !library.isEmpty()) {
                Card card = library.removeFirst();
                gameData.addToExile(controllerId, card);
                gameLogService.append(gameData, GameLog.builder()
                        .text(gameData.playerIdToName.get(controllerId) + " exiles ")
                        .card(card)
                        .text(" from the top of their library ("
                                + ability.sourceCard().getName() + ").")
                        .build());

                List<UUID> exiledCardIds = new ArrayList<>(repeat.exiledCardIds());
                exiledCardIds.add(card.getId());
                int totalManaValue = repeat.totalManaValue() + card.getManaValue();
                if (!beginExileChoice(gameData, ability.sourceCard(), controllerId,
                        exiledCardIds, totalManaValue)) {
                    finish(gameData, ability.sourceCard(), controllerId,
                            exiledCardIds, totalManaValue);
                }
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        finish(gameData, ability.sourceCard(), controllerId,
                repeat.exiledCardIds(), repeat.totalManaValue());
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private boolean beginExileChoice(GameData gameData, Card sourceCard, UUID controllerId,
                                     List<UUID> exiledCardIds, int totalManaValue) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return false;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                controllerId,
                List.of(new DanceWithCalamityEffect(exiledCardIds, totalManaValue)),
                sourceCard.getName() + " - Exile the top card of your library?"));
        return true;
    }

    private void finish(GameData gameData, Card sourceCard, UUID controllerId,
                        List<UUID> exiledCardIds, int totalManaValue) {
        if (totalManaValue > MAX_TOTAL_MANA_VALUE) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                    + " cannot cast the exiled spells because their total mana value is "
                    + totalManaValue + " (" + sourceCard.getName() + ")."));
            return;
        }

        List<UUID> castableSpellIds = exiledCardIds.stream()
                .map(gameData::findExiledCard)
                .filter(java.util.Objects::nonNull)
                .map(exiled -> exiled.card())
                .filter(DanceWithCalamityEffectHandler::isSpell)
                .map(Card::getId)
                .toList();
        if (castableSpellIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds, castableSpellIds.size()));
        log.info("Game {} - {} awaiting Dance with Calamity cast choices for {} exiled spells",
                gameData.id, sourceCard.getName(), castableSpellIds.size());
    }

    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
