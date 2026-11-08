package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetedSpellPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetedSpellPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetedSpellPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenCopyOfTargetedSpellPermanentEffect copyEffect =
                (CreateTokenCopyOfTargetedSpellPermanentEffect) effect;
        StackEntry spell = copyEffect.requiresTargetStillControlled()
                ? CopySupport.triggeringSpell(gameData, entry)
                : entry.getTriggeringSpellSnapshot();
        if (spell == null) return;

        List<UUID> validTargetIds = validTargetIds(gameData, entry, spell, copyEffect.requiresTargetStillControlled());
        if (validTargetIds.isEmpty()) return;

        UUID chosenId = entry.getTargetId();
        if (chosenId == null || !validTargetIds.contains(chosenId)) {
            if (validTargetIds.size() > 1) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.CopyPermanentTargetedBySpell());
                playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validTargetIds,
                        entry.getCard().getName() + " — choose a permanent to copy.");
                return;
            }
            chosenId = validTargetIds.getFirst();
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        StackEntry copyEntry = new StackEntry(entry.getCard(), entry.getControllerId());
        copyEntry.setTargetId(chosenId);
        Permanent lastKnown = departedPermanent(gameData, chosenId);
        if (lastKnown != null && gameQueryService.findPermanentById(gameData, chosenId) == null) {
            copyEntry.rememberLastKnownPermanentCard(chosenId, lastKnown.getCard());
        }
        tokenCopyHandler.resolve(gameData, copyEntry, copyEffect.copyEffect());
    }

    /**
     * The spell's permanent targets that can be copied. With {@code requiresTargetStillControlled} (Orvar, the
     * All-Form's rulings, 2021-02-05) only a target other than the source still on the battlefield under the ability
     * controller's control; otherwise a target that changed control or left the battlefield too, copied as it last
     * existed there (CR 608.2h).
     */
    private List<UUID> validTargetIds(GameData gameData, StackEntry entry, StackEntry spell,
                                      boolean requiresTargetStillControlled) {
        List<UUID> validIds = new ArrayList<>();
        for (UUID targetId : permanentTargetIds(gameData, spell)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            boolean copiable = requiresTargetStillControlled
                    ? target != null && !targetId.equals(entry.getSourcePermanentId())
                    && entry.getControllerId().equals(gameData.findControllerOf(target))
                    : target != null || departedPermanent(gameData, targetId) != null;
            if (copiable) {
                validIds.add(targetId);
            }
        }
        return validIds;
    }

    /** The permanent {@code permanentId} as it last existed on the battlefield, or {@code null} when none was kept. */
    private static Permanent departedPermanent(GameData gameData, UUID permanentId) {
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> snapshot.getId().equals(permanentId))
                .findFirst()
                .orElse(null);
    }

    private List<UUID> permanentTargetIds(GameData gameData, StackEntry spellSnapshot) {
        List<UUID> targetIds = new ArrayList<>(spellSnapshot.getDeclaredTargetIds());
        UUID primaryTargetId = spellSnapshot.getTargetId();
        if (primaryTargetId != null && spellSnapshot.getTargetZone() == null
                && (targetIds.isEmpty() || spellSnapshot.isPrimaryTargetStoredSeparately())) {
            if (spellSnapshot.isPrimaryTargetStoredSeparately()) {
                targetIds.addFirst(primaryTargetId);
            } else {
                targetIds.add(primaryTargetId);
            }
        }
        return targetIds.stream()
                .filter(targetId -> !gameData.playerIds.contains(targetId))
                .distinct()
                .toList();
    }
}
