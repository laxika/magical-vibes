package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves manifesting the top cards of a library (the manifesting player's own, or the library owner's), one at a
 * time (CR 701.40e), and records the MANIFEST step: the
 * permanents manifested and their controllers ("put a +1/+1 counter on it" reads {@code Subjects.thisWay(MANIFEST)}).
 */
@Component
@RequiredArgsConstructor
public class ManifestTopCardEffectHandler implements NormalEffectHandlerBean {

    private final ManifestService manifestService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var manifest = (ManifestTopCardEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, manifest.amount(),
                AmountContext.forStackEntry(entry, null));
        List<UUID> manifested = new ArrayList<>();
        List<UUID> controllers = new ArrayList<>();
        UUID libraryOwnerId = manifest.libraryOwner() == null
                ? null : subjectResolutionService.player(gameData, entry, manifest, manifest.libraryOwner());
        if (manifest.libraryOwner() != null && libraryOwnerId == null) {
            entry.recordStep(StepKind.MANIFEST, StepOutcome.NONE);
            return;
        }
        for (UUID playerId : subjectResolutionService.players(gameData, entry, manifest, manifest.player())) {
            for (int i = 0; i < amount; i++) {
                Permanent permanent = manifestService.manifestTopCard(gameData,
                        libraryOwnerId != null ? libraryOwnerId : playerId, playerId, entry.getCard());
                if (permanent != null) {
                    manifested.add(permanent.getId());
                    controllers.add(playerId);
                }
            }
        }
        entry.recordStep(StepKind.MANIFEST, StepOutcome.ofObjects(manifested, controllers));
    }
}
