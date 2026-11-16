package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CardsStillInZone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutStillExiledCardsIntoGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutStillExiledCardsIntoGraveyardAndCreateTokensEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutStillExiledCardsIntoGraveyardAndCreateTokensEffect.class;
    }

    /**
     * The delayed triggered ability "at the beginning of the next end step, if any of those cards remain exiled, put
     * them into your graveyard, then create a token for each card put into your graveyard this way" that a spell or
     * ability of {@code sourceCard} controlled by {@code controllerId} made after exiling {@code cardIds}.
     */
    public static TemporaryGlobalTriggeredAbility atNextEndStep(GameData gameData, UUID controllerId, Card sourceCard,
                                                                UUID sourcePermanentId, List<UUID> cardIds,
                                                                CreateTokenEffect tokenEffect) {
        Map<UUID, Long> versions = gameData.exileEntryVersionsOf(cardIds);
        return TemporaryGlobalTriggeredAbility.atNextStep(controllerId, sourceCard,
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                        TriggerOptions.DEFAULT.withInterveningIf(new CardsStillInZone(Zone.EXILE, versions)),
                        new PutStillExiledCardsIntoGraveyardAndCreateTokensEffect(versions, tokenEffect)),
                sourcePermanentId, null, null, DelayedReferents.cards(cardIds));
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var graveyardEffect = (PutStillExiledCardsIntoGraveyardAndCreateTokensEffect) effect;
        List<ExiledCardEntry> movedCards = new ArrayList<>();
        for (var remembered : graveyardEffect.exileEntryVersions().entrySet()) {
            UUID cardId = remembered.getKey();
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null
                    || !remembered.getValue().equals(gameData.exileEntryVersions.getOrDefault(cardId, 0L))
                    || !gameData.removeFromExile(cardId)) {
                continue;
            }
            if (graveyardService.addCardToGraveyard(
                    gameData, exiled.ownerId(), exiled.card(), Zone.EXILE)) {
                movedCards.add(exiled);
            }
        }

        if (movedCards.isEmpty()) {
            return;
        }

        permanentControlSupport.applyCreateToken(
                gameData,
                entry.getControllerId(),
                graveyardEffect.tokenEffect(),
                movedCards.size(),
                entry.getCard().getSetCode());
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId()) + " creates "
                        + movedCards.size() + " " + graveyardEffect.tokenEffect().tokenName()
                        + " token(s) for cards put into their graveyard."));
    }
}
