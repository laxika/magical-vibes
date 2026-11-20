package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves {@link GainLifeEffect}: {@link SubjectResolutionService} finds who gains the life, and each of them gains it
 * in APNAP order, with the resolving card as the source of the life gain. The amount is evaluated once against the
 * stack entry (a source-relative amount reads the live source permanent, else its last-known snapshot); for a target
 * player it is evaluated for each target player, with that player as the amount's controller and target.
 */
@Component
@RequiredArgsConstructor
public class GainLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainLifeEffect) effect;
        List<UUID> players = subjectResolutionService.players(gameData, entry, e, e.player());
        Map<UUID, Integer> lifeBefore = new LinkedHashMap<>();
        players.forEach(playerId -> lifeBefore.put(playerId, gameData.getLife(playerId)));
        gainLife(gameData, entry, e, players);
        entry.recordStep(StepKind.GAIN_LIFE, lifeSupport.lifeChange(gameData, lifeBefore, true));
    }

    private void gainLife(GameData gameData, StackEntry entry, GainLifeEffect e, List<UUID> players) {
        if (e.player() instanceof PlayerSubject.TargetPlayer) {
            AmountContext targetContext = AmountContext.forStackEntry(entry, null);
            for (UUID playerId : players) {
                int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                        targetContext.withControllerId(playerId).withTargetPermanentId(playerId));
                gainLife(gameData, entry, playerId, amount);
            }
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        for (UUID playerId : players) {
            gainLife(gameData, entry, playerId, amount);
        }
    }

    private void gainLife(GameData gameData, StackEntry entry, UUID playerId, int amount) {
        lifeSupport.applyGainLife(gameData, playerId, amount, null, entry.getCard(), entry.getEntryType());
    }
}
