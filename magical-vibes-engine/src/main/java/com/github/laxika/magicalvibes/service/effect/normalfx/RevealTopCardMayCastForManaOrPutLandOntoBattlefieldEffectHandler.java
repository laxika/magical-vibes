package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopCardFromLibraryForManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        Card topCard = deck.getFirst();
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ").card(topCard)
                .text(" from the top of their library.").build());

        if (topCard.hasType(CardType.LAND)) {
            deck.removeFirst();
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, new Permanent(topCard));
            battlefieldEntryService.processLandETBEffects(gameData, controllerId, topCard);
            return;
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Top-card cast effect is not present on its stack entry");
        }

        CastTopCardFromLibraryForManaEffect castEffect =
                new CastTopCardFromLibraryForManaEffect(topCard, e.manaCost());
        entry.replaceEffectToResolve(effectIndex,
                new MayPayManaEffect(e.manaCost(), castEffect,
                        "Pay " + e.manaCost() + " to cast " + topCard.getName() + "?"));
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                controllerId,
                List.of(castEffect),
                entry.getCard().getName() + " — Cast " + topCard.getName()
                        + " by paying " + e.manaCost() + "?",
                null,
                e.manaCost(),
                entry.getSourcePermanentId()
        ));
        log.info("Game {} - {} may cast {} from the top of the library by paying {}",
                gameData.id, playerName, topCard.getName(), e.manaCost());
    }
}
