package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.effect.l.LivingDeathEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Living Death: each player exiles all creature cards from their graveyard, then sacrifices all
 * creatures they control, then puts all cards they exiled this way onto the battlefield.
 *
 * <p>The exile step runs first, so creatures sacrificed by the second step hit their owners'
 * graveyards after the exile snapshot was taken and are not reanimated. The returning cards all
 * enter simultaneously (CR 614.12), so none of them sees the others entering.
 */
@Component
@RequiredArgsConstructor
public class LivingDeathEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final DestructionSupport destructionSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LivingDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, List<Card>> exiledByPlayer = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }

            List<Card> creatureCards = graveyard.stream()
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .toList();
            List<Card> exiled = new ArrayList<>();
            for (Card card : creatureCards) {
                if (graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, card.getId(), card)) {
                    exiled.add(card);
                }
            }
            exiledByPlayer.put(playerId, exiled);
        }

        List<UUID> creatureIdsToSacrifice = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureIdsToSacrifice.add(permanent.getId());
            }
        });
        destructionSupport.performSimultaneousSacrifice(gameData, creatureIdsToSacrifice);

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        Map<Permanent, UUID> controllerByPermanent = new LinkedHashMap<>();
        for (Map.Entry<UUID, List<Card>> playerEntry : exiledByPlayer.entrySet()) {
            UUID playerId = playerEntry.getKey();
            for (Card card : playerEntry.getValue()) {
                if (!gameData.removeFromExile(card.getId())) {
                    continue;
                }
                Permanent permanent = new Permanent(card);
                permanent.setEnteredFromExile(true);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, playerId, permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                controllerByPermanent.put(permanent, playerId);
            }
        }

        for (Map.Entry<Permanent, UUID> permanentEntry : controllerByPermanent.entrySet()) {
            Permanent permanent = permanentEntry.getKey();
            graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                    gameData, permanentEntry.getValue(), permanent, permanent.getCard());
        }
    }
}
