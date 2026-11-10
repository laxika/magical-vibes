package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Pia's Revolution's damage-or-return choice. */
@Component
@RequiredArgsConstructor
public class ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        Card artifact = gameQueryService.findCardInGraveyardById(gameData, entry.getTriggeringCardId());
        String artifactName = artifact != null ? artifact.getName() : "the artifact";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), entry.getControllerId(), List.of(e),
                "Have " + entry.getCard().getName() + " deal " + e.damage()
                        + " damage to you to keep " + artifactName + " in its owner's graveyard?",
                targetPlayerId, null, entry.getSourcePermanentId(), null,
                0, 0, null, null, targetPlayerId, entry.getSourcePermanentSnapshot(),
                entry.getControllerId(), entry.getTriggeringCardId(), 0)
                .withTriggeringCard(entry.getTriggeringCardId(), entry.getTriggeringCardGraveyardEntryVersion()));
    }

    public void returnArtifactToOwnerHand(GameData gameData, UUID artifactId, long graveyardEntryVersion) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, artifactId);
        Card artifact = gameQueryService.findCardInGraveyardById(gameData, artifactId);
        // "That card" is the graveyard object the ability triggered for: once it left the graveyard and came back it is
        // a new object, which isn't returned (CR 400.7).
        if (artifact == null || ownerId == null
                || graveyardEntryVersion >= 0 && gameData.graveyardEntryVersion(artifactId) != graveyardEntryVersion) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, artifactId);
        permanentRemovalService.addCardToHandFromGraveyard(gameData, ownerId, ownerId, artifact);
        gameLogService.append(gameData, GameLog.builder()
                .card(artifact)
                .text(" returns from graveyard to " + gameData.playerIdToName.get(ownerId) + "'s hand.")
                .build());
    }
}
