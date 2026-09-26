package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GandalfWestwardVoyagerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GandalfWestwardVoyagerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final CopyControllerCastSpellEffectHandler copyControllerCastSpellEffectHandler;
    private final EachOpponentDrawsCardEffectHandler eachOpponentDrawsCardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GandalfWestwardVoyagerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry triggeringSpell = gameQueryService.findStackEntryByCardId(
                gameData, entry.getTriggeringCardId());
        if (triggeringSpell == null || triggeringSpell.getCard() == null) {
            return;
        }

        Card spellCard = triggeringSpell.getCard();
        boolean sharesCardType = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (entry.getControllerId().equals(playerId)) {
                continue;
            }

            List<Card> library = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (library == null || library.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty (Gandalf, Westward Voyager)."));
                continue;
            }

            Card topCard = library.getFirst();
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " reveals ", topCard,
                    " from the top of their library (Gandalf, Westward Voyager)."));
            sharesCardType |= sharesCardType(gameData, spellCard, topCard,
                    triggeringSpell.getControllerId(), playerId);
        }

        if (!sharesCardType) {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
            return;
        }

        copyControllerCastSpellEffectHandler.resolve(gameData, entry,
                new CopyControllerCastSpellEffect(
                        new StackEntry(triggeringSpell), triggeringSpell.getControllerId(),
                        Set.of(), Set.of(), isPermanentSpell(triggeringSpell.getEntryType()), true));
        eachOpponentDrawsCardEffectHandler.resolve(gameData, entry, new EachOpponentDrawsCardEffect(1));
    }

    private boolean sharesCardType(GameData gameData, Card spellCard, Card topCard,
                                   UUID spellControllerId, UUID libraryOwnerId) {
        for (CardType type : CardType.values()) {
            if (gameQueryService.cardHasType(spellCard, type, gameData, spellControllerId)
                    && gameQueryService.cardHasType(topCard, type, gameData, libraryOwnerId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPermanentSpell(StackEntryType entryType) {
        return switch (entryType) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case INSTANT_SPELL, SORCERY_SPELL, TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
    }
}
