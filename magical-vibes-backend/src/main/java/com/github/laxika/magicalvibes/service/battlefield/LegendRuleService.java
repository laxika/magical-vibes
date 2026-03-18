package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
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
            if (perm.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)) {
                legendaryByName.computeIfAbsent(perm.getCard().getName(), k -> new ArrayList<>()).add(perm.getId());
            }
        }

        for (Map.Entry<String, List<UUID>> entry : legendaryByName.entrySet()) {
            if (entry.getValue().size() >= 2) {
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.LegendRule(entry.getKey()));
                playerInputService.beginPermanentChoice(gameData, controllerId, entry.getValue(),
                        "You control multiple legendary permanents named " + entry.getKey() + ". Choose one to keep.");
                return true;
            }
        }
        return false;
    }
}

