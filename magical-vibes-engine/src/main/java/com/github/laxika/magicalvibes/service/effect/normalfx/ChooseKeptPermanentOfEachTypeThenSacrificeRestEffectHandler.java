package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
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
 * <p>Walks the affected players in APNAP order and, for each, the listed card types in order. Each
 * pass lets the spell's controller or the affected player pick one permanent of that type; a type
 * with a single candidate is auto-kept and a type with none is skipped. A permanent that has
 * several listed types can be picked again in a later pass, so kept permanents stay in the candidate
 * lists. Once every pass is done, the applicable permanents that were not kept are sacrificed
 * simultaneously.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseKeptPermanentOfEachTypeThenSacrificeRestEffectHandler implements NormalEffectHandlerBean {

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
        ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect keepEffect =
                (ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect) effect;
        List<UUID> affectedPlayerIds = affectedPlayers(gameData, entry, keepEffect.recipient());
        step(gameData, entry.getControllerId(), affectedPlayerIds, 0, List.of(),
                keepEffect.types(), keepEffect.sacrificeAllPermanents(), keepEffect.eachPlayerChooses(),
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
                context.types().indexOf(context.typePhase()) + 1, kept, context.types(),
                context.sacrificeAllPermanents(), context.eachPlayerChooses(), context.sourceName());
    }

    /**
     * Run passes from {@code phaseIndex} of the first player in {@code playerIds} onwards, stopping
     * at the first pass that needs a choice; when no pass is left, apply the sacrifices.
     */
    private void step(GameData gameData, UUID controllerId, List<UUID> playerIds, int phaseIndex,
            List<UUID> keptIds, List<CardType> types, boolean sacrificeAllPermanents,
            boolean eachPlayerChooses, String sourceName) {
        List<UUID> remaining = new ArrayList<>(playerIds);
        List<UUID> kept = new ArrayList<>(keptIds);
        int index = phaseIndex;

        while (!remaining.isEmpty()) {
            UUID subjectPlayerId = remaining.getFirst();
            while (index < types.size()) {
                CardType type = types.get(index);
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

                UUID chooserId = eachPlayerChooses ? subjectPlayerId : controllerId;
                playerInputService.beginMultiPermanentChoice(gameData, chooserId, candidates, 1,
                        new MultiPermanentChoiceContext.KeepOneOfEachTypeChoice(controllerId,
                                subjectPlayerId, type, List.copyOf(remaining), List.copyOf(kept), sourceName,
                                List.copyOf(types), sacrificeAllPermanents, eachPlayerChooses),
                        sourceName + " — choose the " + type.name().toLowerCase() + " "
                                + gameData.playerIdToName.get(subjectPlayerId) + " keeps.");
                return;
            }
            remaining.removeFirst();
            index = 0;
        }

        sacrificeRest(gameData, playerIds, kept, sacrificeAllPermanents, sourceName);
    }

    /** Every affected permanent that was not kept, or every affected nonland permanent for Tragic Arrogance, is sacrificed together. */
    private void sacrificeRest(GameData gameData, List<UUID> affectedPlayerIds, List<UUID> keptIds,
            boolean sacrificeAllPermanents,
            String sourceName) {
        Set<UUID> keptSet = new HashSet<>(keptIds);
        List<UUID> toSacrifice = new ArrayList<>();
        for (UUID playerId : affectedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if ((sacrificeAllPermanents || !gameQueryService.isLand(gameData, permanent))
                        && !keptSet.contains(permanent.getId())) {
                    toSacrifice.add(permanent.getId());
                }
            }
        }

        if (toSacrifice.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but nothing is sacrificed."));
            return;
        }
        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        log.info("Game {} - {} sacrifices {} permanents", gameData.id, sourceName, toSacrifice.size());
    }

    private List<UUID> candidates(GameData gameData, UUID playerId, CardType type) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> matchesType(gameData, permanent, type));
    }

    private boolean matchesType(GameData gameData, Permanent permanent, CardType type) {
        return switch (type) {
            case ARTIFACT -> gameQueryService.isArtifact(gameData, permanent);
            case BATTLE -> gameQueryService.isBattle(gameData, permanent);
            case CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
            case LAND -> gameQueryService.isLand(gameData, permanent);
            case PLANESWALKER -> gameQueryService.isPlaneswalker(gameData, permanent);
            default -> false;
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

    private List<UUID> affectedPlayers(GameData gameData, StackEntry entry, SacrificeRecipient recipient) {
        return switch (recipient) {
            case EACH_PLAYER, EACH_OPPONENT -> apnapPlayers(gameData).stream()
                    .filter(playerId -> recipient == SacrificeRecipient.EACH_PLAYER
                            || !playerId.equals(entry.getControllerId()))
                    .filter(playerId -> gameQueryService.canEffectCauseSacrifice(
                            gameData, playerId, entry.getControllerId()))
                    .toList();
            default -> throw new IllegalArgumentException(
                    "ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect requires EACH_PLAYER or EACH_OPPONENT");
        };
    }
}
