package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetOpponentCreatesTokenEffect}: the targeted opponent (a gift: the controller's opponent)
 * creates the tokens under their own control.
 */
@Component
public class TargetOpponentCreatesTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    public TargetOpponentCreatesTokenEffectHandler(
            PermanentControlSupport permanentControlSupport,
            GameQueryService gameQueryService,
            AmountEvaluationService amountEvaluationService,
            SubjectResolutionService subjectResolutionService) {
        this.permanentControlSupport = permanentControlSupport;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.subjectResolutionService = subjectResolutionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetOpponentCreatesTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetOpponentCreatesTokenEffect) effect;

        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }
        if (e.gift()) {
            // The gift is given even if no token is created (Jolly Gerbils' ruling).
            ActionEvents.giftAbilityResolved(gameData, entry);
        }
        UUID opponentId;
        if (e.creator() != null) {
            opponentId = subjectResolutionService.player(gameData, entry, effect, e.creator());
        } else if (entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())) {
            // "Target opponent creates": the opponent the ability targets (Phelddagrif), none once that target is
            // illegal (CR 608.2b).
            List<UUID> targeted = subjectResolutionService.players(gameData, entry, effect,
                    Subjects.targetPlayerDeclaredElsewhere());
            opponentId = targeted.isEmpty() ? null : targeted.getFirst();
        } else {
            // A gift's opponent is not recorded (the owner's open question): the first other seat.
            opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        }
        if (opponentId == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        var context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), context);
        if (amount <= 0) {
            return;
        }

        int power = amountEvaluationService.evaluate(gameData, e.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.token().toughness(), context);
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(gameData, opponentId, e.token(),
                amount, entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);
    }
}
