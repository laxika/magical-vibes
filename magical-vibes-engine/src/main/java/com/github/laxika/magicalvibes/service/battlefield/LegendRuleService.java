package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.GlobalLegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsLegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesLegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledSubtypeLegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledTokensLegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledNameCountLegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.LegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleWhenExactlyTwoSameNameEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Enforces the legend rule (CR 704.5j): if a player controls two or more legendary permanents
 * with the same name, that player chooses one and puts the rest into the graveyard.
 *
 * <p>This service detects the first such violation for a given player and prompts them to choose
 * which legendary permanent to keep. Only one violation is processed at a time; subsequent
 * violations are handled on the next state-based action check.
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
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;

        Map<String, List<UUID>> legendaryByName = new HashMap<>();
        for (Permanent perm : battlefield) {
            if (isLegendary(gameData, perm)) {
                legendaryByName.computeIfAbsent(gameQueryService.getEffectiveName(gameData, perm), k -> new ArrayList<>()).add(perm.getId());
            }
        }

        for (Map.Entry<String, List<UUID>> entry : legendaryByName.entrySet()) {
            if (entry.getValue().size() >= 2 && !hasGlobalExemption(gameData)
                    && !hasControlledPermanentsExemption(gameData, controllerId)
                    && !allExempt(gameData, battlefield, entry.getKey(), controllerId)) {
                List<UUID> nonExemptPermanents = entry.getValue().stream()
                        .filter(id -> findPermanent(battlefield, id)
                                .map(perm -> !hasControlledSubtypeExemption(gameData, battlefield, perm)
                                        && !hasControlledCreaturesExemption(gameData, controllerId, perm)
                                        && !hasControlledTokensExemption(gameData, controllerId, perm))
                                .orElse(false))
                        .toList();
                if (nonExemptPermanents.size() < 2) {
                    continue;
                }
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.LegendRule(entry.getKey(), nonExemptPermanents));
                playerInputService.beginPermanentChoice(gameData, controllerId, nonExemptPermanents,
                        "You control multiple legendary permanents named " + entry.getKey() + ". Choose one to keep.");
                return true;
            }
        }
        return false;
    }

    private boolean hasGlobalExemption(GameData gameData) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .anyMatch(perm -> perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(GlobalLegendRuleExemptionEffect.class::isInstance));
    }

    private boolean hasControlledPermanentsExemption(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield != null && battlefield.stream()
                .flatMap(perm -> perm.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(ControlledPermanentsLegendRuleExemptionEffect.class::isInstance);
    }

    /**
     * Whether active exemptions protect the same-named permanents. The exactly-two exemption
     * protects both permanents while either one retains the ability, and counts across all
     * players' battlefields. Other exemptions are evaluated on each affected permanent.
     */
    private boolean allExempt(GameData gameData, List<Permanent> battlefield, String name,
                              UUID controllerId) {
        int totalWithName = countOnBattlefield(gameData, name);
        if (gameData.playerBattlefields.values().stream().flatMap(List::stream)
                .filter(permanent -> name.equals(gameQueryService.getEffectiveName(gameData, permanent)))
                .flatMap(permanent -> gameQueryService.getActiveStaticEffects(gameData, permanent).stream())
                .anyMatch(effect -> effect instanceof IgnoreLegendRuleWhenExactlyTwoSameNameEffect exemption
                        && exemption.exemptFromLegendRule(totalWithName))) {
            return true;
        }
        return battlefield.stream()
                .filter(perm -> name.equals(gameQueryService.getEffectiveName(gameData, perm)))
                .allMatch(perm -> hasLegendRuleExemption(gameData, perm, name, totalWithName,
                        controllerId));
    }

    private boolean hasLegendRuleExemption(GameData gameData, Permanent permanent, String name,
                                            int totalWithName, UUID controllerId) {
        int controlledWithName = countControlledOnBattlefield(gameData, controllerId, name);
        return gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                .anyMatch(effect -> effect instanceof LegendRuleExemptionEffect
                        && ((LegendRuleExemptionEffect) effect).exemptFromLegendRule(totalWithName)
                        || effect instanceof ControlledNameCountLegendRuleExemptionEffect
                        && ((ControlledNameCountLegendRuleExemptionEffect) effect)
                        .exemptFromLegendRule(controlledWithName));
    }

    private int countOnBattlefield(GameData gameData, String name) {
        int count = 0;
        for (List<Permanent> permanents : gameData.playerBattlefields.values()) {
            for (Permanent perm : permanents) {
                if (name.equals(gameQueryService.getEffectiveName(gameData, perm))) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countControlledOnBattlefield(GameData gameData, UUID controllerId, String name) {
        return (int) gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(perm -> name.equals(gameQueryService.getEffectiveName(gameData, perm)))
                .count();
    }

    private java.util.Optional<Permanent> findPermanent(List<Permanent> battlefield, UUID id) {
        return battlefield.stream().filter(perm -> perm.getId().equals(id)).findFirst();
    }

    private boolean hasControlledSubtypeExemption(GameData gameData, List<Permanent> battlefield,
                                                  Permanent permanent) {
        var effectiveSubtypes = gameQueryService.effectiveCreatureSubtypes(gameData, permanent);
        return battlefield.stream()
                .flatMap(source -> source.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(ControlledSubtypeLegendRuleExemptionEffect.class::isInstance)
                .map(ControlledSubtypeLegendRuleExemptionEffect.class::cast)
                .anyMatch(exemption -> effectiveSubtypes.contains(exemption.exemptedSubtype()));
    }

    private boolean hasControlledCreaturesExemption(GameData gameData, UUID controllerId,
                                                    Permanent permanent) {
        if (!gameQueryService.isCreature(gameData, permanent)) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield != null && battlefield.stream()
                .flatMap(source -> gameQueryService.getActiveStaticEffects(gameData, source).stream())
                .anyMatch(ControlledCreaturesLegendRuleExemptionEffect.class::isInstance);
    }

    private boolean hasControlledTokensExemption(GameData gameData, UUID controllerId,
                                                 Permanent permanent) {
        if (!permanent.getCard().isToken()) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield != null && battlefield.stream()
                .flatMap(source -> source.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(ControlledTokensLegendRuleExemptionEffect.class::isInstance);
    }

    /**
     * Checks whether a permanent is legendary, considering both its natural supertypes
     * and any supertypes granted by static effects (e.g. In Bolas's Clutches).
     */
    private boolean isLegendary(GameData gameData, Permanent perm) {
        return gameQueryService.hasEffectiveSupertype(gameData, perm, CardSupertype.LEGENDARY);
    }
}
