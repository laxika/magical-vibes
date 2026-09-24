package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OtherControlledTokensBecomeCopyOfChosenTokenUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Brudiclad's non-targeting token-copy choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtherControlledTokensBecomeCopyOfChosenTokenUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private static final PermanentPredicate OWN_TOKENS = new PermanentAllOfPredicate(List.of(
            new PermanentIsTokenPredicate(),
            new PermanentControlledBySourceControllerPredicate()));

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffectHandler
            copyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OtherControlledTokensBecomeCopyOfChosenTokenUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> tokens = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        if (tokens.isEmpty()) {
            return;
        }
        if (tokens.size() == 1) {
            applyCopy(gameData, entry, tokens.getFirst().getId());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.BrudicladTokenChoice(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId,
                tokens.stream().map(Permanent::getId).toList(),
                entry.getCard().getName() + " - Choose a token you control to copy.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.BrudicladTokenChoice context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(
                        gameData, chosenPermanentId))
                || !chosen.getCard().isToken()) {
            throw new IllegalStateException("Chosen permanent is not a token you control");
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Brudiclad resolution is no longer pending");
        }
        applyCopy(gameData, pendingEntry, chosenPermanentId);
    }

    private void applyCopy(GameData gameData, StackEntry entry, UUID chosenPermanentId) {
        UUID previousTargetId = entry.getTargetId();
        entry.setTargetIdForEffectResolution(chosenPermanentId);
        try {
            copyHandler.resolve(gameData, entry,
                    new EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect(
                            OWN_TOKENS, OWN_TOKENS));
        } finally {
            entry.restoreTargetIdAfterEffectResolution(previousTargetId);
        }
    }
}
