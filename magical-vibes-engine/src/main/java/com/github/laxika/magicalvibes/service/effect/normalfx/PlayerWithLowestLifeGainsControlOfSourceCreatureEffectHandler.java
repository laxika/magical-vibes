package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithLowestLifeGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlayerWithLowestLifeGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayerWithLowestLifeGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = findSource(gameData, entry.getCard().getId());
        if (source == null) {
            return;
        }

        int lowestLife = gameData.orderedPlayerIds.stream()
                .mapToInt(gameData::getLife)
                .min()
                .orElse(0);
        List<UUID> tiedPlayers = gameData.orderedPlayerIds.stream()
                .filter(playerId -> gameData.getLife(playerId) == lowestLife)
                .toList();

        if (tiedPlayers.size() == 1) {
            giveControl(gameData, entry, source, tiedPlayers.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PlayerWithLowestLifeChoice(entry.getCard()));
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), tiedPlayers,
                entry.getCard().getName() + " — Choose one of the players tied for lowest life total.");
    }

    public void giveControl(GameData gameData, StackEntry entry, Permanent source, UUID playerId) {
        creatureControlService.applyControlEffect(gameData, playerId, source,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT, null, entry.getCard().getName());
    }

    private Permanent findSource(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(cardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
