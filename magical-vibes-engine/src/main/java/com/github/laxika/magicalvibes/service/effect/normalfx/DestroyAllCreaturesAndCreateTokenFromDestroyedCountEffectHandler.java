package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect) effect;
        List<Permanent> toDestroy = new ArrayList<>();
                gameData.forEachBattlefield((playerId, battlefield) -> {
                    for (Permanent perm : battlefield) {
                        if (gameQueryService.isCreature(gameData, perm)) {
                            toDestroy.add(perm);
                        }
                    }
                });

                // Snapshot indestructible before any removals
                Set<Permanent> indestructible = new HashSet<>();
                for (Permanent perm : toDestroy) {
                    if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                        indestructible.add(perm);
                    }
                }

                // Destroy and count
                int destroyedCount = 0;
                String sourceName = entry.getCard().getName();
                for (Permanent perm : toDestroy) {
                    if (indestructible.contains(perm)) {
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName() + " is indestructible."));
                        continue;
                    }
                    if (graveyardService.tryRegenerate(gameData, perm)) {
                        continue;
                    }
                    permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName() + " is destroyed."));
                    log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
                    destroyedCount++;
                }

                // Create token with P/T = destroyed count (doubled by token creation replacement effects)
                UUID controllerId = entry.getControllerId();
                int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
                Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
                enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

                for (int copy = 0; copy < tokenMultiplier; copy++) {
                    Card tokenCard = new Card();
                    tokenCard.setName(e.tokenName());
                    tokenCard.setType(CardType.CREATURE);
                    tokenCard.setManaCost("");
                    tokenCard.setToken(true);
                    tokenCard.setColor(null);
                    tokenCard.setPower(destroyedCount);
                    tokenCard.setToughness(destroyedCount);
                    tokenCard.setSubtypes(e.tokenSubtypes());
                    if (e.tokenAdditionalTypes() != null && !e.tokenAdditionalTypes().isEmpty()) {
                        tokenCard.setAdditionalTypes(e.tokenAdditionalTypes());
                    }

                    Permanent tokenPermanent = new Permanent(tokenCard);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

                    String logEntry = "A " + destroyedCount + "/" + destroyedCount + " " + e.tokenName()
                            + " artifact creature token enters the battlefield.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} creates a {}/{} {} artifact creature token",
                            gameData.id, sourceName, destroyedCount, destroyedCount, e.tokenName());

                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
                }
    }
}
