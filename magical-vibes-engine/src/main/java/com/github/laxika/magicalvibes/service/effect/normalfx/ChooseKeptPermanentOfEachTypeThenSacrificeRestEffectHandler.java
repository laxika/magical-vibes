package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect} (Tragic Arrogance).
 *
 * <p>Walks the players in APNAP order and, for each, the four card types in printed order. Each pass
 * lets the spell's controller pick one permanent of that type the player controls; a type with a
 * single candidate is auto-kept and a type with none is skipped. A permanent that has several of the
 * four types can be picked again in a later pass, so kept permanents stay in the candidate lists.
 * Once every pass is done, all nonland permanents that were not kept are sacrificed simultaneously.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseKeptPermanentOfEachTypeThenSacrificeRestEffectHandler implements NormalEffectHandlerBean {

    private static final List<CardType> TYPES = List.of(
            CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.PLANESWALKER);

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, entry.getControllerId(), apnapPlayers(gameData), 0, List.of(),
                entry.getCard().getName());
    }

    /** Choice completion: record the pick, then continue with that player's next type pass. */
    public void completeKeepChoice(GameData gameData, List<UUID> chosenIds,
            MultiPermanentChoiceContext.KeepOneOfEachTypeChoice context) {
        List<UUID> kept = new ArrayList<>(context.keptIds());
        for (UUID chosen : chosenIds) {
            if (!kept.contains(chosen)) {
                kept.add(chosen);
            }
            Permanent permanent = gameQueryService.findPermanentById(gameData, chosen);
            if (permanent != null) {
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(context.subjectPlayerId()) + " keeps ",
                        permanent.getCard(), " (" + context.sourceName() + ")."));
            }
        }

        step(gameData, context.controllerId(), context.remainingPlayerIds(),
                TYPES.indexOf(context.typePhase()) + 1, kept, context.sourceName());
    }

    /**
     * Run passes from {@code phaseIndex} of the first player in {@code playerIds} onwards, stopping
     * at the first pass that needs a choice; when no pass is left, apply the sacrifices.
     */
    private void step(GameData gameData, UUID controllerId, List<UUID> playerIds, int phaseIndex,
            List<UUID> keptIds, String sourceName) {
        List<UUID> remaining = new ArrayList<>(playerIds);
        List<UUID> kept = new ArrayList<>(keptIds);
        int index = phaseIndex;

        while (!remaining.isEmpty()) {
            UUID subjectPlayerId = remaining.getFirst();
            while (index < TYPES.size()) {
                CardType type = TYPES.get(index);
                List<UUID> candidates = candidates(gameData, subjectPlayerId, type);
                index++;

                if (candidates.isEmpty()) {
                    continue;
                }
                if (candidates.size() == 1) {
                    UUID only = candidates.getFirst();
                    if (!kept.contains(only)) {
                        kept.add(only);
                    }
                    continue;
                }

                playerInputService.beginMultiPermanentChoice(gameData, controllerId, candidates, 1,
                        new MultiPermanentChoiceContext.KeepOneOfEachTypeChoice(controllerId,
                                subjectPlayerId, type, List.copyOf(remaining), List.copyOf(kept), sourceName),
                        sourceName + " — choose the " + type.name().toLowerCase() + " "
                                + gameData.playerIdToName.get(subjectPlayerId) + " keeps.");
                return;
            }
            remaining.removeFirst();
            index = 0;
        }

        sacrificeRest(gameData, kept, sourceName);
    }

    /** Every nonland permanent that was not kept is sacrificed by its controller, all at once. */
    private void sacrificeRest(GameData gameData, List<UUID> keptIds, String sourceName) {
        Set<UUID> keptSet = new HashSet<>(keptIds);
        List<UUID> toSacrifice = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isLand(gameData, permanent) && !keptSet.contains(permanent.getId())) {
                    toSacrifice.add(permanent.getId());
                }
            }
        });

        if (toSacrifice.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but nothing is sacrificed."));
            return;
        }
        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        log.info("Game {} - {} sacrifices {} nonland permanents", gameData.id, sourceName, toSacrifice.size());
    }

    private List<UUID> candidates(GameData gameData, UUID playerId, CardType type) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> matchesType(gameData, permanent, type));
    }

    private boolean matchesType(GameData gameData, Permanent permanent, CardType type) {
        return switch (type) {
            case ARTIFACT -> gameQueryService.isArtifact(gameData, permanent);
            case CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
            default -> gameQueryService.isPlaneswalker(gameData, permanent);
        };
    }

    /** All players, active player first. */
    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
