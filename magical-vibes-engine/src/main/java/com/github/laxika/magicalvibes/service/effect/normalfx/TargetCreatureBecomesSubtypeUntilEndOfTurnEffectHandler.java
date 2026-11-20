package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureBecomesSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureBecomesSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreatureBecomesSubtypeUntilEndOfTurnEffect) effect;
        if (e.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            if (subjectResolutionService.players(gameData, entry, effect, Subjects.targetPlayer()).isEmpty()) {
                return;
            }
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    e.scope().subject())) {
                setCreatureTypeOverride(gameData, entry, permanent, e.subtypes());
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) into " + e.subtype().getDisplayName()
                            + "s until end of turn.").build());
            return;
        }

        if (e.scope() == GrantScope.OWN_CREATURES) {
            int count = 0;
            // This one-shot "creatures you control" includes the source.
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    GrantScope.ALL_OWN_CREATURES.subject())) {
                setCreatureTypeOverride(gameData, entry, permanent, e.subtypes());
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) into " + e.subtype().getDisplayName()
                            + "s until end of turn.").build());
            return;
        }

        Permanent target = subjectResolutionService.permanent(gameData, entry, effect, GrantScope.TARGET.subject());
        if (target == null) {
            return;
        }
        setCreatureTypeOverride(gameData, entry, target, e.subtypes());
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" becomes a "
                + subtypeNames(e.subtypes()) + " until end of turn.").build());
    }

    /**
     * Registers the change as timestamped floating layer-4 effects (CR 613.1d, 613.7), so a type-adding
     * effect that begins later (Xenograft) is not stripped by this one.
     */
    private void setCreatureTypeOverride(GameData gameData, StackEntry entry, Permanent permanent,
                                         List<CardSubtype> subtypes) {
        for (int i = 0; i < subtypes.size(); i++) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new GrantSubtypeEffect(subtypes.get(i), GrantScope.TARGET, i == 0),
                    permanent.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
        }
    }

    private String subtypeNames(List<CardSubtype> subtypes) {
        return subtypes.stream()
                .map(CardSubtype::getDisplayName)
                .reduce((left, right) -> left + " " + right)
                .orElseThrow();
    }
}
