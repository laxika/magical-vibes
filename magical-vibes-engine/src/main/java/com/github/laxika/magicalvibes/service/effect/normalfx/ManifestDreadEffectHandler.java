package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves manifest dread for the source-linked exiled card's owner, an effect controller, or a targeted spell's controller. */
@Component
@RequiredArgsConstructor
public class ManifestDreadEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestDreadEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ManifestDreadEffect manifestDreadEffect = (ManifestDreadEffect) effect;
        UUID libraryOwnerId;
        if (manifestDreadEffect.useTargetSpellControllerLibrary()) {
            StackEntry targetSpell = gameData.stack.stream()
                    .filter(stackEntry -> stackEntry.getTargetableId().equals(entry.getTargetId()))
                    .findFirst()
                    .orElse(null);
            libraryOwnerId = targetSpell != null
                    ? targetSpell.getControllerId() : entry.getCounteredSpellControllerId();
            if (libraryOwnerId == null) {
                return;
            }
        } else if (manifestDreadEffect.useControllerLibrary()) {
            libraryOwnerId = entry.getControllerId();
        } else if (manifestDreadEffect.libraryOwnerId() != null) {
            libraryOwnerId = manifestDreadEffect.libraryOwnerId();
        } else {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId == null) {
                return;
            }

            ExiledCardEntry exiledCard = findExiledCard(gameData, sourcePermanentId);
            if (exiledCard == null) {
                return;
            }
            libraryOwnerId = exiledCard.ownerId();
            // Each player who owns a card exiled with the source manifests dread in turn (2024-09-20 ruling
            // when the enters ability resolved more than once); the rest are queued after this effect.
            List<CardEffect> otherOwners = findOtherOwners(gameData, sourcePermanentId, libraryOwnerId).stream()
                    .<CardEffect>map(ownerId -> new ManifestDreadEffect(false, false, ownerId))
                    .toList();
            if (!otherOwners.isEmpty()) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, otherOwners);
            }
        }

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, libraryOwnerId, 2, true);
        if (result == null) {
            // CR 701.62b: "whenever you manifest dread" abilities trigger even when the library
            // had no cards to look at, so the process completes with nothing put into the graveyard.
            triggerCollectionService.checkManifestDreadTriggers(gameData, libraryOwnerId, null);
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.LibraryRevealChoice.manifestDread(
                result.controllerId(), result.topCards(),
                "Choose one of these cards to manifest. Put the other into your graveyard."));
    }

    private List<UUID> findOtherOwners(GameData gameData, UUID sourcePermanentId, UUID firstOwnerId) {
        synchronized (gameData.exiledCards) {
            return gameData.exiledCards.stream()
                    .filter(exiled -> sourcePermanentId.equals(exiled.sourcePermanentId()))
                    .map(ExiledCardEntry::ownerId)
                    .filter(ownerId -> !ownerId.equals(firstOwnerId))
                    .distinct()
                    .toList();
        }
    }

    private ExiledCardEntry findExiledCard(GameData gameData, UUID sourcePermanentId) {
        synchronized (gameData.exiledCards) {
            return gameData.exiledCards.stream()
                    .filter(exiled -> sourcePermanentId.equals(exiled.sourcePermanentId()))
                    .findFirst()
                    .orElse(null);
        }
    }
}
