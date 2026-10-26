package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
    private final TokenCopySupport tokenCopySupport;

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

        if (e.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                targetCard, e.filter(), entry.getCard().getId(), gameData,
                gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId()),
                null, null, entry.getXValue(), entry.getSourcePermanentSnapshot())) {
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

        if (e.targetPutIntoGraveyardFromAnywhereThisTurn()
                && (graveyardOwnerId == null
                || !gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                .getOrDefault(graveyardOwnerId, Set.of()).contains(targetCard.getId()))) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target was not put into a graveyard this turn)."));
            return;
        }

        if (e.exileTargetCard()) {
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCard.getId());
            if (graveyardOwnerId != null) {
                if (e.trackWithSource() && entry.getSourcePermanentId() != null) {
                    exileService.exileCard(gameData, graveyardOwnerId, targetCard, entry.getSourcePermanentId());
                } else {
                    exileService.exileCard(gameData, graveyardOwnerId, targetCard);
                }
            }
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " exiles ", targetCard, " from a graveyard."));

        if (e.exileOtherControlledTokensOfSubtype() == null) {
            createOrderedTokenCopy(gameData, entry, e, targetCard);
            return;
        }

        int createdPermanentCount = entry.getCreatedPermanentIds().size();
        graveyardReturnSupport.createTokenCopyFromCard(gameData, entry, targetCard, e.additionalSubtypes(),
                false, e.exileAtEndStep(), e.colorOverride(),
                e.powerOverride(), e.toughnessOverride(), e.replaceSubtypes(), false,
                new ArrayList<>(), e.additionalKeywords(), false, e.removeLegendary(),
                e.additionalTypes(), e.sacrificeAtEndStep(), e.tappedAndAttacking(),
                e.exileAtEndOfCombat());
        if (e.grantHaste()) {
            for (UUID createdId : entry.getCreatedPermanentIds().subList(createdPermanentCount,
                    entry.getCreatedPermanentIds().size())) {
                Permanent token = gameQueryService.findPermanentById(gameData, createdId);
                if (token != null) {
                    token.getPersistentGrantedKeywords().add(Keyword.HASTE);
                }
            }
        }

        if (e.exileOtherControlledTokensOfSubtype() != null) {
            Set<UUID> createdByThisEffect = new HashSet<>(entry.getCreatedPermanentIds()
                    .subList(createdPermanentCount, entry.getCreatedPermanentIds().size()));
            PermanentPredicate filter = new PermanentAllOfPredicate(List.of(
                    new PermanentIsTokenPredicate(),
                    new PermanentHasSubtypePredicate(e.exileOtherControlledTokensOfSubtype()),
                    new PermanentControlledBySourceControllerPredicate()));
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard().getId())
                    .withSourceControllerId(entry.getControllerId());
            List<Permanent> toExile = new ArrayList<>();
            for (Permanent permanent : gameData.playerBattlefields
                    .getOrDefault(entry.getControllerId(), List.of())) {
                if (!createdByThisEffect.contains(permanent.getId())
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext)) {
                    toExile.add(permanent);
                }
            }
            for (Permanent permanent : toExile) {
                permanentRemovalService.removePermanentToExile(gameData, permanent);
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
            }
            permanentRemovalService.removeOrphanedAuras(gameData);
        }

        if (e.createZombieTokenWithExiledCardStats()) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), zombieWithStatsOf(gameData, targetCard), 1,
                    entry.getCard().getSetCode()));
        }
    }

    /**
     * Creates the copy as one token creation event whose count replacements and additions the controller orders
     * (CR 616.1). The creation may pause for that choice (or Jinnie Fay's), so Soul Separator's Zombie is spliced in
     * as the entry's next step, where it waits for the copy and is its own creation event.
     */
    private void createOrderedTokenCopy(GameData gameData, StackEntry entry,
                                        ExileTargetCardFromGraveyardAndCreateTokenCopyEffect e, Card targetCard) {
        List<CardSubtype> additionalSubtypes = e.additionalSubtypes() == null ? List.of() : e.additionalSubtypes();
        var copyOptions = new CreateTokenCopyOfTargetPermanentEffect(
                e.replaceSubtypes() ? List.of() : additionalSubtypes,
                e.additionalTypes() == null ? Set.of() : e.additionalTypes(),
                e.powerOverride(), e.toughnessOverride(), Map.of(), e.grantHaste(), e.exileAtEndStep(),
                e.sacrificeAtEndStep(), e.tappedAndAttacking(), false, false, e.colorOverride(),
                e.additionalKeywords() == null ? Set.of() : e.additionalKeywords(), false, Map.of(), List.of(),
                false, e.removeLegendary(), new Fixed(1), e.exileAtEndOfCombat(), Set.of(), false, List.of(),
                null, false, false);
        List<UUID> attackTargetIds = null;
        if (e.tappedAndAttacking()) {
            Permanent sourcePermanent = entry.getSourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            UUID attackTargetId = sourcePermanent == null
                    ? entry.getAttackedTargetId() : sourcePermanent.getAttackTarget();
            attackTargetIds = attackTargetId == null ? null : List.of(attackTargetId);
        }
        if (e.createZombieTokenWithExiledCardStats()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(zombieWithStatsOf(gameData, targetCard)));
        }
        Consumer<Card> copyException = e.replaceSubtypes()
                ? tokenCard -> tokenCopySupport.replaceCreatureTypes(tokenCard, additionalSubtypes)
                : null;
        if (attackTargetIds != null) {
            tokenCopySupport.createTokenCopies(gameData, entry, List.of(targetCard), null,
                    entry.getControllerId(), copyOptions, attackTargetIds);
        } else {
            tokenCopySupport.createTokenCopiesWithCopyException(gameData, entry, List.of(targetCard), null,
                    entry.getControllerId(), copyOptions, copyException);
        }
    }

    /** Soul Separator's second token: a black Zombie whose P/T are the exiled card's P/T. */
    private CreateTokenEffect zombieWithStatsOf(GameData gameData, Card exiledCard) {
        Integer zombiePower = gameQueryService.getEffectiveCardPower(gameData, exiledCard);
        Integer zombieToughness = gameQueryService.getEffectiveCardToughness(gameData, exiledCard);
        return new CreateTokenEffect("Zombie",
                zombiePower == null ? 0 : zombiePower, zombieToughness == null ? 0 : zombieToughness,
                CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of());
    }
}
