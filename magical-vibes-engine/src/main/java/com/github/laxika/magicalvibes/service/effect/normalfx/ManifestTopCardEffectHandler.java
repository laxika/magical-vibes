package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves manifesting the top cards of a library, one at a time (CR 701.40e). */
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
        for (UUID playerId : subjectResolutionService.players(gameData, entry, manifest, manifest.player())) {
            for (int i = 0; i < amount; i++) {
                manifestService.manifestTopCard(gameData, playerId, entry.getCard());
            }
        }
    }
}
