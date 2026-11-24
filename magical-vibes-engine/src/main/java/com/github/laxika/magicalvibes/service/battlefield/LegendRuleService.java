package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleWhenExactlyTwoSameNameEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Enforces the legend rule (CR 704.5j): if a player controls two or more legendary permanents
 * with the same name, that player chooses one and puts the rest into the graveyard.
 *
 * <p>This service detects the first such violation for a given player and prompts them to choose
 * which legendary permanent to keep. Only one violation is processed at a time; subsequent
 * violations are handled on the next state-based action check.
 *
 * <p>{@code StateBasedActionService.performStateBasedActions} runs the check for every player, so no path
 * that puts a permanent onto the battlefield or changes its controller has to call it. The check opens a
 * player choice, so the state-based pass skips it while another interaction is open or queued and finds the
 * violation again on its next run.
 */
@Service
@RequiredArgsConstructor
public class LegendRuleService {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;

    /**
     * Checks whether the given player controls two or more legendary permanents with the same name.
     * If a violation is found, the player is prompted to choose one to keep; the rest will be put
     * into the graveyard upon selection.
     *
     * @param gameData     the current game state
     * @param controllerId the player whose battlefield to inspect
     * @return {@code true} if a legend rule violation was detected and the player is awaiting a
     *         choice, {@code false} if no violation exists
     */
    public boolean checkLegendRule(GameData gameData, UUID controllerId) {
        Map.Entry<String, List<UUID>> violation = findLegendRuleViolation(gameData, controllerId);
        if (violation == null) {
            return false;
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.LegendRule(violation.getKey(), violation.getValue()));
        playerInputService.beginPermanentChoice(gameData, controllerId, violation.getValue(),
                "You control multiple legendary permanents named " + violation.getKey() + ". Choose one to keep.");
        return true;
    }

    /**
     * Whether the given player currently controls legendary permanents that the legend rule would make
     * them choose between. State-based actions are performed before a triggered ability's targets are
     * chosen as it is put on the stack, so callers use this to hold such targeting back until the
     * violation has been resolved.
     */
    public boolean hasLegendRuleViolation(GameData gameData, UUID controllerId) {
        return findLegendRuleViolation(gameData, controllerId) != null;
    }

    private Map.Entry<String, List<UUID>> findLegendRuleViolation(GameData gameData, UUID controllerId) {
        return gameQueryService.withQueryScope(gameData, () -> findLegendRuleViolationInScope(gameData, controllerId));
    }

    /** {@link #findLegendRuleViolation}, in one query scope: it changes nothing. */
    private Map.Entry<String, List<UUID>> findLegendRuleViolationInScope(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return null;

        Map<String, List<Permanent>> legendaryByName = new LinkedHashMap<>();
        for (Permanent perm : battlefield) {
            if (isLegendary(gameData, perm)) {
                legendaryByName.computeIfAbsent(gameQueryService.getEffectiveName(gameData, perm),
                        k -> new ArrayList<>()).add(perm);
            }
        }
        Exemptions exemptions = null;
        for (Map.Entry<String, List<Permanent>> entry : legendaryByName.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            if (exemptions == null) {
                exemptions = exemptions(gameData);
            }
            Exemptions found = exemptions;
            List<UUID> nonExemptPermanents = entry.getValue().stream()
                    .filter(perm -> !found.exempts(gameData, perm, controllerId, gameQueryService))
                    .map(Permanent::getId)
                    .toList();
            if (nonExemptPermanents.size() >= 2) {
                return Map.entry(entry.getKey(), nonExemptPermanents);
            }
        }
        return null;
    }

    /**
     * The legend-rule exemptions in force: each active {@link IgnoreLegendRuleEffect} with its source, and the
     * permanents an active {@link IgnoreLegendRuleWhenExactlyTwoSameNameEffect} exempts because its set names exactly
     * two permanents with its source's name.
     */
    private Exemptions exemptions(GameData gameData) {
        List<GameQueryService.ActiveStaticEffect<IgnoreLegendRuleEffect>> ignores =
                gameQueryService.activeStaticEffects(gameData, IgnoreLegendRuleEffect.class);
        Set<UUID> exactlyTwo = new HashSet<>();
        for (GameQueryService.ActiveStaticEffect<IgnoreLegendRuleWhenExactlyTwoSameNameEffect> active
                : gameQueryService.activeStaticEffects(gameData, IgnoreLegendRuleWhenExactlyTwoSameNameEffect.class)) {
            String name = gameQueryService.getEffectiveName(gameData, active.source());
            if (name == null) {
                continue;
            }
            List<UUID> named = new ArrayList<>();
            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Permanent perm : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    if (name.equals(gameQueryService.getEffectiveName(gameData, perm))
                            && gameQueryService.staticSubjectNames(gameData, active.source(), active.controllerId(),
                            playerId, perm, active.effect().counted())) {
                        named.add(perm.getId());
                    }
                }
            }
            if (named.size() == 2) {
                exactlyTwo.addAll(named);
            }
        }
        return new Exemptions(ignores, exactlyTwo);
    }

    /** The exemptions {@link #exemptions} found, asked about one permanent at a time. */
    private record Exemptions(List<GameQueryService.ActiveStaticEffect<IgnoreLegendRuleEffect>> ignores,
                              Set<UUID> exactlyTwo) {

        /** Whether the legend rule doesn't apply to {@code permanent}, which {@code controllerId} controls. */
        boolean exempts(GameData gameData, Permanent permanent, UUID controllerId, GameQueryService gameQueryService) {
            return exactlyTwo.contains(permanent.getId()) || ignores.stream().anyMatch(active ->
                    gameQueryService.staticSubjectNames(gameData, active.source(), active.controllerId(), controllerId,
                            permanent, active.effect().permanents()));
        }
    }

    /**
     * Checks whether a permanent is legendary, considering both its natural supertypes
     * and any supertypes granted by static effects (e.g. In Bolas's Clutches).
     */
    private boolean isLegendary(GameData gameData, Permanent perm) {
        return gameQueryService.hasEffectiveSupertype(gameData, perm, CardSupertype.LEGENDARY);
    }
}
