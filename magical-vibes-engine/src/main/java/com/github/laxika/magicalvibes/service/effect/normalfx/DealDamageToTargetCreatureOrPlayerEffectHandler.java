package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlayerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves creature-or-player damage through the shared any-target damage pipeline. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureOrPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToAnyTargetEffectHandler delegate;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureOrPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToTargetCreatureOrPlayerEffect) effect;
        if (damageEffect.chooseOnResolution() && entry.getChosenPermanentId() == null) {
            List<UUID> creatures = new ArrayList<>();
            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    if (gameQueryService.isCreature(gameData, permanent)) creatures.add(permanent.getId());
                }
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChosenPermanentReference());
            playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), creatures,
                    gameData.orderedPlayerIds, "Choose a creature or player to receive damage.");
            return;
        }
        UUID previousTargetId = entry.getTargetId();
        if (damageEffect.chooseOnResolution()) entry.setTargetId(entry.getChosenPermanentId());
        try {
            delegate.resolve(gameData, entry, new DealDamageToAnyTargetEffect(damageEffect.damage()));
        } finally {
            if (damageEffect.chooseOnResolution()) entry.setTargetId(previousTargetId);
        }
    }
}
