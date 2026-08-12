package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardAndCreateTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardAndCreateTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetCardFromGraveyardAndCreateTokenCopyEffect) effect;

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
        if (targetCard == null && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetCardIds().getFirst());
        }
        if (targetCard == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        if (e.filter() != null && !predicateEvaluationService.matchesCardPredicate(targetCard, e.filter(), null)) {
            String filterLabel = CardPredicateUtils.describeFilter(e.filter());
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target is no longer a valid " + filterLabel + ")."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (e.ownGraveyardOnly() && graveyardOwnerId != null
                && !graveyardOwnerId.equals(entry.getControllerId())) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target is not in your graveyard)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCard.getId());
        if (graveyardOwnerId != null) {
            exileService.exileCard(gameData, graveyardOwnerId, targetCard);
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " exiles ", targetCard, " from a graveyard."));

        graveyardReturnSupport.createTokenCopyFromCard(gameData, entry, targetCard, e.additionalSubtypes(),
                e.additionalKeywords(), e.grantHaste(), e.exileAtEndStep(), e.colorOverride(),
                e.powerOverride(), e.toughnessOverride());

        // Soul Separator's second token: a black Zombie whose P/T are the exiled card's printed P/T.
        if (e.createZombieTokenWithExiledCardStats()) {
            CreateTokenEffect zombie = new CreateTokenEffect("Zombie", targetCard.getPower(), targetCard.getToughness(),
                    CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of());
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), zombie, 1, entry.getCard().getSetCode()));
        }
    }
}
