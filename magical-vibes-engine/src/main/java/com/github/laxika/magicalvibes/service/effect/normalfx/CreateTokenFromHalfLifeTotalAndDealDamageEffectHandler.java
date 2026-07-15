package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenFromHalfLifeTotalAndDealDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import com.github.laxika.magicalvibes.carddata.scryfall.ScryfallOracleLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenFromHalfLifeTotalAndDealDamageEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenFromHalfLifeTotalAndDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenFromHalfLifeTotalAndDealDamageEffect) effect;
        
                UUID controllerId = entry.getControllerId();
                int currentLife = gameData.getLife(controllerId);
                int x = (currentLife + 1) / 2; // half life total, rounded up
                if (x < 0) x = 0;

                // Create the X/X token
                Card tokenCard = new Card();
                tokenCard.setName(e.tokenName());
                tokenCard.setType(CardType.CREATURE);
                tokenCard.setManaCost("");
                tokenCard.setToken(true);
                tokenCard.setColor(e.color());
                tokenCard.setPower(x);
                tokenCard.setToughness(x);
                tokenCard.setSubtypes(e.subtypes());

                ScryfallOracleLoader.TokenImageData imageData = ScryfallOracleLoader.getTokenImage(
                        entry.getCard().getSetCode(), e.tokenName(), x, x, e.color()
                );
                if (imageData != null) {
                    tokenCard.setSetCode(imageData.setCode());
                    tokenCard.setCollectorNumber(imageData.collectorNumber());
                }

                Permanent tokenPerm = new Permanent(tokenCard);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPerm);

                String tokenLog = "A " + x + "/" + x + " black " + e.tokenName() + " creature token enters the battlefield.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(tokenLog));
                log.info("Game {} - {} {}/{} token created for {}", gameData.id, e.tokenName(), x, x, controllerId);

                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);

                // The token deals X damage to the controller (damage source is the token, not the Saga)
                if (x > 0) {
                    if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
                        String playerName = gameData.playerIdToName.get(controllerId);
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s life total can't change."));
                    } else {
                        int life = gameData.getLife(controllerId);
                        gameData.playerLifeTotals.put(controllerId, life - x);
                        String dmgLog = e.tokenName() + " deals " + x + " damage to " + gameData.playerIdToName.get(controllerId) + ".";
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(dmgLog));
                        log.info("Game {} - {} deals {} damage to controller {}", gameData.id, e.tokenName(), x, controllerId);
                        triggerCollectionService.checkLifeLossTriggers(gameData, controllerId, x);
                    }
                }
    
    }
}
