package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HazoretsUndyingFuryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HazoretsUndyingFuryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HazoretsUndyingFuryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (HazoretsUndyingFuryEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " shuffles their library (" + sourceName + ")."));

        List<UUID> exiledThisProcess = new ArrayList<>();
        for (int i = 0; i < e.exileCount() && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            gameData.addToExile(controllerId, card);
            exiledThisProcess.add(card.getId());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " exiles ").card(card).text(" (" + sourceName + ").").build());
        }

        List<UUID> castableSpellIds = new ArrayList<>();
        for (UUID cardId : exiledThisProcess) {
            Card exiled = gameData.findExiledCard(cardId).card();
            if (isCastableSpell(exiled) && exiled.getManaValue() <= e.maxCastableManaValue()) {
                castableSpellIds.add(cardId);
            }
        }

        if (castableSpellIds.isEmpty()) {
            log.info("Game {} - {} found no castable spells (mana value {} or less) among exiled cards",
                    gameData.id, sourceName, e.maxCastableManaValue());
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds, castableSpellIds.size()));
        log.info("Game {} - {} awaiting cast choices for {} exiled spells",
                gameData.id, sourceName, castableSpellIds.size());
    }

    private static boolean isCastableSpell(Card card) {
        return card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)
                || card.hasType(CardType.CREATURE) || card.hasType(CardType.ARTIFACT)
                || card.hasType(CardType.ENCHANTMENT) || card.hasType(CardType.PLANESWALKER);
    }
}
