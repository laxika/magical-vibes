package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves manifest dread for the source-linked exiled card's owner, an effect controller, or a targeted spell's controller. */
@Component
@RequiredArgsConstructor
public class ManifestDreadEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

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
        }

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, libraryOwnerId, 2, true);
        if (result == null) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.LibraryRevealChoice.manifestDread(
                result.controllerId(), result.topCards(),
                "Choose one of these cards to manifest. Put the other into your graveyard."));
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
