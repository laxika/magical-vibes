package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LegendRuleService {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    public boolean checkLegendRule(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;

        Map<String, List<UUID>> legendaryByName = new HashMap<>();
        for (Permanent perm : battlefield) {
            if (perm.getCard().getSupertypes().contains(com.github.laxika.magicalvibes.model.CardSupertype.LEGENDARY)) {
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

