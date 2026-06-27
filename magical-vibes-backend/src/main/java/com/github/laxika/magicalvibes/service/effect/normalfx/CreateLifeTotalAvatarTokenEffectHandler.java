package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateLifeTotalAvatarTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControllerLifeTotalEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateLifeTotalAvatarTokenEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateLifeTotalAvatarTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateLifeTotalAvatarTokenEffect) effect;
        
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
                    tokenCard.setColor(e.color());
                    tokenCard.setPower(0);
                    tokenCard.setToughness(0);
                    tokenCard.setSubtypes(e.subtypes());

                    // CDA: "This creature's power and toughness are each equal to your life total."
                    tokenCard.addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControllerLifeTotalEffect());

                    ScryfallOracleLoader.TokenImageData imageData = ScryfallOracleLoader.getTokenImage(
                            entry.getCard().getSetCode(), e.tokenName(), 0, 0, e.color()
                    );
                    if (imageData != null) {
                        tokenCard.setSetCode(imageData.setCode());
                        tokenCard.setCollectorNumber(imageData.collectorNumber());
                    }

                    Permanent tokenPermanent = new Permanent(tokenCard);
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

                    int lifeTotal = gameData.playerLifeTotals.getOrDefault(controllerId, 0);
                    String logEntry = entry.getCard().getName() + " creates a " + lifeTotal + "/" + lifeTotal
                            + " white " + e.tokenName() + " creature token.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} creates a {}/{} {} token", gameData.id, entry.getCard().getName(),
                            lifeTotal, lifeTotal, e.tokenName());
                }
    
    }
}
