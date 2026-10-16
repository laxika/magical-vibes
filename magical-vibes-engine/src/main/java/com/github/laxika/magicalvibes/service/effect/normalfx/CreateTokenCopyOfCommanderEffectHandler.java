package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCommanderEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Resolves I Am Never Alone's commander-wide token-copy effect. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfCommanderEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfCommanderEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> commanders = gameData.playerCommanders.getOrDefault(entry.getControllerId(), List.of());
        if (commanders.isEmpty()) {
            return;
        }

        if (commanders.size() > 1) {
            playerInputService.beginCommandZoneCardChoice(
                    gameData, entry.getControllerId(), commanders,
                    "Choose a commander to create a token copy of.", entry.getCard());
            return;
        }
        createTokenCopyOfCommander(gameData, entry, commanders.getFirst());
    }

    /** Completes the commander choice made when the controller has several commanders. */
    public void completeChoice(GameData gameData, UUID playerId, UUID commanderId, Card sourceCard) {
        Card commander = gameData.playerCommanders.getOrDefault(playerId, List.of()).stream()
                .filter(card -> card.getId().equals(commanderId))
                .findFirst()
                .orElse(null);
        if (commander == null) {
            return;
        }
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, playerId,
                sourceCard == null ? "" : sourceCard.getName(), List.of());
        createTokenCopyOfCommander(gameData, entry, commander);
    }

    private void createTokenCopyOfCommander(GameData gameData, StackEntry entry, Card commander) {
        Permanent commanderPermanent = null;
        Card sourceCard = null;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getOriginalCard() != null
                        && commander.getId().equals(permanent.getOriginalCard().getId())) {
                    commanderPermanent = permanent;
                    sourceCard = permanent.getCard();
                    break;
                }
            }
            if (sourceCard != null) {
                break;
            }
        }
        if (sourceCard == null) {
            sourceCard = gameQueryService.findCardById(gameData, commander.getId());
            // CR 707.2: a copy of an object on the stack acquires the value of X chosen for it.
            for (StackEntry stackEntry : gameData.stack) {
                if (stackEntry.getCard() != null && commander.getId().equals(stackEntry.getCard().getId())) {
                    entry.setTokenCopyXValue(stackEntry.getXValue());
                    break;
                }
            }
        }
        if (sourceCard == null) {
            sourceCard = commander;
        }
        if (commanderPermanent != null && commanderPermanent.isFaceDown()) {
            Card faceDownCharacteristics = new Card();
            faceDownCharacteristics.setName("");
            faceDownCharacteristics.setManaCost("");
            faceDownCharacteristics.setType(CardType.CREATURE);
            faceDownCharacteristics.setPower(commanderPermanent.getFaceDownPower());
            faceDownCharacteristics.setToughness(commanderPermanent.getFaceDownToughness());
            sourceCard = faceDownCharacteristics;
        }

        tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                List.of(sourceCard),
                commanderPermanent,
                entry.getControllerId(),
                CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                        List.of(), Set.of(), null, null, Map.of()));
    }
}
