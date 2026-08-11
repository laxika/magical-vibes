package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceCardFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnSourceCardFromGraveyardToBattlefieldEffect) effect;

        Card card = entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            log.info("Game {} - {} graveyard return fizzles (no longer in a graveyard)", gameData.id, card.getName());
            return;
        }

        // Grafdigger's Cage etc.: creature cards in graveyards can't enter the battlefield.
        if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.cardThen(card, " can't return from the graveyard; it stays in the graveyard."));
            log.info("Game {} - {} graveyard return blocked (can't enter from a graveyard)", gameData.id, card.getName());
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        if (e.tapped()) {
            permanent.tap();
        }
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " returns ", card,
                " to the battlefield" + (e.tapped() ? " tapped" : "") + "."));
        log.info("Game {} - {} returns to the battlefield from the graveyard", gameData.id, card.getName());

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, ownerId, permanent, card);

        if (e.losesAllAbilities()) {
            permanent.setLosesAllAbilitiesPermanently(true);
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), card.getName(), null,
                    entry.getControllerId(), new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.PERMANENT),
                    permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        }
    }
}
