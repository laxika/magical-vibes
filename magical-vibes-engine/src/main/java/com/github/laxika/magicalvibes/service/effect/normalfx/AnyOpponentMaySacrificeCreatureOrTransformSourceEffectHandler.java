package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureOrTransformSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the upkeep trigger of Innocent Traveler. Opponents choose in turn order, then all
 * chosen creatures are sacrificed together before the source transforms when nobody chose one.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeCreatureOrTransformSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final TransformSelfEffectHandler transformSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMaySacrificeCreatureOrTransformSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> opponents = new ArrayList<>(
                AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(gameData, controllerId));
        opponents.removeIf(id -> creatureIds(gameData, id, controllerId).isEmpty());

        if (opponents.isEmpty()) {
            finish(gameData, entry.getCard(), controllerId, entry.getSourcePermanentId(), List.of());
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyOpponentMaySacrificeCreatureOrTransformSourceEffect(
                List.copyOf(opponents), controllerId, entry.getSourcePermanentId(), List.of()));
    }

    /** The creatures an opponent can legally sacrifice for this ability. */
    public List<UUID> creatureIds(GameData gameData, UUID playerId, UUID abilityControllerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, abilityControllerId)) {
            return List.of();
        }
        return destructionSupport.collectCreatureIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent));
    }

    /** Enqueues the may prompt for the first remaining opponent. */
    public void promptNext(GameData gameData, Card sourceCard,
            AnyOpponentMaySacrificeCreatureOrTransformSourceEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Sacrifice a creature? If no one does, " + sourceCard.getName() + " transforms.",
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
        log.info("Game {} - offering {} the {} sacrifice choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    /** Records the choice, advances to the next opponent, or completes the trigger. */
    public void advance(GameData gameData, Card sourceCard,
            AnyOpponentMaySacrificeCreatureOrTransformSourceEffect effect,
            UUID chooserId, UUID chosenCreatureId) {
        List<UUID> chosen = new ArrayList<>(effect.chosenCreatureIds());
        if (chosenCreatureId != null) {
            chosen.add(chosenCreatureId);
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(chooserId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        remaining.removeIf(id -> creatureIds(gameData, id, effect.abilityControllerId()).isEmpty());

        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyOpponentMaySacrificeCreatureOrTransformSourceEffect(
                    List.copyOf(remaining), effect.abilityControllerId(), effect.sourcePermanentId(), chosen));
            return;
        }

        finish(gameData, sourceCard, effect.abilityControllerId(), effect.sourcePermanentId(), chosen);
    }

    private void finish(GameData gameData, Card sourceCard, UUID abilityControllerId,
                        UUID sourcePermanentId, List<UUID> chosenCreatureIds) {
        if (!chosenCreatureIds.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, chosenCreatureIds);
            return;
        }

        if (sourcePermanentId == null) {
            return;
        }
        StackEntry transformEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                abilityControllerId,
                sourceCard.getName() + "'s ability",
                List.of(new TransformSelfEffect()),
                null,
                sourcePermanentId);
        transformSelfEffectHandler.resolve(gameData, transformEntry, new TransformSelfEffect());
    }
}
