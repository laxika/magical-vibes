package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TeachEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeachEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final TeachSupport teachSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TeachEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.isCopy()) {
            return;
        }

        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    validIds.add(permanent.getId());
                }
            }
        }
        if (validIds.isEmpty()) {
            teachSupport.teachWithoutChoice(gameData, entry, entry.getEffectsToResolve().indexOf(effect) + 1);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.Teach());
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validIds,
                entry.getCard().getName() + " — Choose a creature to teach this spell to.");
    }
}
