package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfCreaturesThenReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the creature-tapping choice used by reflexive abilities. */
@Component
@RequiredArgsConstructor
public class TapAnyNumberOfCreaturesThenReflexiveAbilityEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapAnyNumberOfCreaturesThenReflexiveAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TapAnyNumberOfCreaturesThenReflexiveAbilityEffect tapEffect =
                (TapAnyNumberOfCreaturesThenReflexiveAbilityEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> eligibleIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!permanent.isTapped() && gameQueryService.isCreature(gameData, permanent)) {
                    eligibleIds.add(permanent.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" resolves, but there are no untapped creatures to tap.").build());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                new MultiPermanentChoiceContext.TapCreaturesThenQueueReflexiveAbility(
                        entry, tapEffect.reflexiveEffect()),
                "Tap any number of untapped creatures you control.");
    }
}
