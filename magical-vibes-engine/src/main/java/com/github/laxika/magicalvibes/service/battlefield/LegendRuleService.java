package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.GlobalLegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledSubtypeLegendRuleExemptionEffect;
import com.github.laxika.magicalvibes.model.effect.LegendRuleExemptionEffect;
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
                legendaryByName.computeIfAbsent(perm.getCard().getName(), k -> new ArrayList<>()).add(perm.getId());
            }
        }

        for (Map.Entry<String, List<UUID>> entry : legendaryByName.entrySet()) {
            if (entry.getValue().size() >= 2 && !hasGlobalExemption(gameData)
                    && !allExempt(gameData, battlefield, entry.getKey())) {
                List<UUID> nonExemptPermanents = entry.getValue().stream()
                        .filter(id -> findPermanent(battlefield, id)
                                .map(perm -> !hasControlledSubtypeExemption(gameData, battlefield, perm))
                                .orElse(false))
                        .toList();
                if (nonExemptPermanents.size() < 2) {
                    continue;
                }
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.LegendRule(entry.getKey()));
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

    /**
     * Whether every permanent with {@code name} on this battlefield carries a currently-active
     * {@link LegendRuleExemptionEffect} (Brothers Yamazaki). The count handed to the exemption spans
     * all players' battlefields, because the wordings that grant it ("if there are exactly two
     * permanents named ~ on the battlefield") are not controller-scoped.
     */
    private boolean allExempt(GameData gameData, List<Permanent> battlefield, String name) {
        int totalWithName = countOnBattlefield(gameData, name);
        return battlefield.stream()
                .filter(perm -> name.equals(perm.getCard().getName()))
                .allMatch(perm -> perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(LegendRuleExemptionEffect.class::isInstance)
                        .map(LegendRuleExemptionEffect.class::cast)
                        .anyMatch(exemption -> exemption.exemptFromLegendRule(totalWithName)));
    }

    private int countOnBattlefield(GameData gameData, String name) {
        int count = 0;
        for (List<Permanent> permanents : gameData.playerBattlefields.values()) {
            for (Permanent perm : permanents) {
                if (name.equals(perm.getCard().getName())) {
                    count++;
                }
            }
        }
        return count;
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

    /**
     * Checks whether a permanent is legendary, considering both its natural supertypes
     * and any supertypes granted by static effects (e.g. In Bolas's Clutches).
     */
    private boolean isLegendary(GameData gameData, Permanent perm) {
        if (perm.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)) {
            return true;
        }
        return gameQueryService.computeStaticBonus(gameData, perm)
                .grantedSupertypes().contains(CardSupertype.LEGENDARY);
    }
}
