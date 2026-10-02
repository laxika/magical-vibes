package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Skyline Savior's non-targeting bounce and perpetual Angel upgrade. */
@Component
@RequiredArgsConstructor
public class ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffect) effect;
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId());

        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(controllerId)
                            + " controls no " + e.permanentDescription() + " to return."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.BouncePermanentAndPerpetuallyBecomeAngel(
                        controllerId, entry.getCard(), entry.getSourcePermanentId()));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose a " + e.permanentDescription()
                        + " to return to its owner's hand.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.BouncePermanentAndPerpetuallyBecomeAngel context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        Card card = target.getCard();
        UUID cardOwnerId = card.getOwnerId() == null ? context.controllerId() : card.getOwnerId();
        boolean upgrade = !card.isToken()
                && gameQueryService.cardHasType(card, CardType.CREATURE, gameData, cardOwnerId)
                && !gameQueryService.cardHasSubtype(card, CardSubtype.ANGEL, gameData, cardOwnerId);

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            if (upgrade) {
                PerpetualCardPowerToughnessSupport.remember(gameData, card, 1, 1);
                gameData.perpetualCardKeywords
                        .computeIfAbsent(card.getId(), ignored -> EnumSet.noneOf(Keyword.class))
                        .add(Keyword.FLYING);
                gameData.perpetualCardSubtypes.merge(card.getId(),
                        EnumSet.of(CardSubtype.ANGEL), (existing, added) -> {
                            EnumSet<CardSubtype> merged = EnumSet.noneOf(CardSubtype.class);
                            merged.addAll(existing);
                            merged.addAll(added);
                            return Set.copyOf(merged);
                        });
            }
            String message = " is returned to its owner's hand.";
            if (upgrade) {
                message += " It perpetually gets +1/+1, gains flying, and becomes an Angel"
                        + " in addition to its other types.";
            }
            gameLogService.append(gameData, GameLog.cardThen(card, message));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
