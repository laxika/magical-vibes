package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.carddata.scryfall.ScryfallOracleLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LivingWeaponEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LivingWeaponEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                UUID controllerId = entry.getControllerId();
                int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
                Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
                enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

                Permanent lastTokenPermanent = null;
                for (int copy = 0; copy < tokenMultiplier; copy++) {
                    // Create a 0/0 black Phyrexian Germ creature token
                    Card tokenCard = new Card();
                    tokenCard.setName("Phyrexian Germ");
                    tokenCard.setType(CardType.CREATURE);
                    tokenCard.setManaCost("");
                    tokenCard.setToken(true);
                    tokenCard.setColor(CardColor.BLACK);
                    tokenCard.setPower(0);
                    tokenCard.setToughness(0);
                    tokenCard.setSubtypes(List.of(CardSubtype.PHYREXIAN, CardSubtype.GERM));

                    // Look up token image from Scryfall token set
                    // Scryfall names this token "Germ" (subtypes are Phyrexian Germ)
                    ScryfallOracleLoader.TokenImageData germImageData = ScryfallOracleLoader.getTokenImage(
                            entry.getCard().getSetCode(), "Germ", 0, 0, CardColor.BLACK
                    );
                    if (germImageData != null) {
                        tokenCard.setSetCode(germImageData.setCode());
                        tokenCard.setCollectorNumber(germImageData.collectorNumber());
                    }

                    Permanent tokenPermanent = new Permanent(tokenCard);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

                    String logEntry = "A 0/0 black Phyrexian Germ creature token enters the battlefield.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
                    if (!gameData.interaction.isAwaitingInput()) {
                        legendRuleService.checkLegendRule(gameData, controllerId);
                    }

                    lastTokenPermanent = tokenPermanent;
                }

                // Attach the equipment to the last token created (per CR 614.6b)
                if (lastTokenPermanent != null) {
                    Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                    if (equipment != null) {
                        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
                        equipment.setAttachedTo(lastTokenPermanent.getId());
                        // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
                        equipment.setTimestamp(gameData.nextTimestamp());
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), " is now attached to Phyrexian Germ."));
                        log.info("Game {} - {} attached to Phyrexian Germ token via living weapon", gameData.id, entry.getCard().getName());
                    }
                }

                log.info("Game {} - Living weapon: {} Phyrexian Germ token(s) created for player {}", gameData.id, tokenMultiplier, controllerId);
    
    }
}
