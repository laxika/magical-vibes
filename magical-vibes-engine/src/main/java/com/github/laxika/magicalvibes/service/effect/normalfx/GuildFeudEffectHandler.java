package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GuildFeudEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Guild Feud's upkeep ability by starting the two-stage reveal flow owned by
 * {@link GuildFeudSupport} (targeted opponent first, then the controller, then the fight).
 */
@Component
@RequiredArgsConstructor
public class GuildFeudEffectHandler implements NormalEffectHandlerBean {

    private final GuildFeudSupport guildFeudSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GuildFeudEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        guildFeudSupport.begin(gameData, entry, targetId);
    }
}
