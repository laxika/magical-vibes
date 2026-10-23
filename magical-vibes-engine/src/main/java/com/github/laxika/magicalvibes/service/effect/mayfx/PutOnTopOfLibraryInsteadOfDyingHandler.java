package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutOnTopOfLibraryInsteadOfDyingHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutOnTopOfLibraryInsteadOfDyingEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (ability.sourcePermanentId() != null
                && permanentRemovalService.recordRerunMayLibraryChoice(gameData, ability.sourcePermanentId(), accepted)) {
            // Asked before a stopped effect or choice completion kills it; the rerun moves it.
            if (gameData.pendingDeathReplacementChoiceReplay != null
                    && !permanentRemovalService.awaitsDeathReplacementChoiceBeforeRerun(gameData)) {
                permanentChoiceHandlerServiceProvider.getObject().completeChoiceAgainAfterDeathReplacements(gameData);
                return;
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        Permanent target = ability.sourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (target != null && !permanentRemovalService.recordStateBasedMayLibraryChoice(gameData, target, accepted)) {
            if (accepted) {
                permanentRemovalService.removePermanentToLibraryTop(gameData, target);
            } else {
                permanentRemovalService.removePermanentToGraveyardAfterDecliningLibraryReplacement(gameData, target);
            }
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
