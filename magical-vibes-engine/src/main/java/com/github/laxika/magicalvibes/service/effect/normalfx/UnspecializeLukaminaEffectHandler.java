package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.l.LukaminaMoonDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnspecializeLukaminaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/** Returns a dead specialized Lukamina to the battlefield as her base face. */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnspecializeLukaminaEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnspecializeLukaminaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card specialized = entry.getSourcePermanentSnapshot() != null
                && entry.getSourcePermanentSnapshot().getOriginalCard() != null
                ? entry.getSourcePermanentSnapshot().getOriginalCard()
                : entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, specialized.getId());
        if (ownerId == null) {
            log.info("Game {} - {} unspecialize return fizzles (no longer in a graveyard)",
                    gameData.id, specialized.getName());
            return;
        }
        if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, specialized, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.cardThen(specialized,
                    " can't return from the graveyard; it stays in the graveyard."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, specialized.getId());

        Card unspecialized = specialized.createRuntimeCopy();
        unspecialized.clearRulesTextAndAbilities();
        LukaminaMoonDruid.setBaseFaceCharacteristics(unspecialized);
        LukaminaMoonDruid.addBaseAbilities(unspecialized);

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(unspecialized);
        permanent.tap();
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " returns ", unspecialized,
                " to the battlefield unspecialized and tapped."));
        log.info("Game {} - {} returns to the battlefield unspecialized and tapped",
                gameData.id, specialized.getName());
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, ownerId, permanent, unspecialized);
    }
}
