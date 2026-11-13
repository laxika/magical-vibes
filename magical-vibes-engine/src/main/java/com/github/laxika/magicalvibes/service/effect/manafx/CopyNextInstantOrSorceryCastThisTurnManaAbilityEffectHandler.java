package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CopyNextInstantOrSorceryCastThisTurnManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextInstantOrSorceryCastThisTurnEffect.class;
    }

    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        int manaMultiplier, boolean creatureSource) {
        resolve(gameData, playerId, permanent, new CopyNextInstantOrSorceryCastThisTurnEffect());
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        resolve(gameData, playerId, permanent, (CopyNextInstantOrSorceryCastThisTurnEffect) effect);
    }

    /**
     * Makes the mana ability's delayed triggered ability (CR 603.7), its source the mana ability's (CR 603.7e). A mana
     * ability resolves on no stack, so each activation's ability is its own ({@code resolution} 0).
     */
    private void resolve(GameData gameData, UUID playerId, Permanent permanent,
                         CopyNextInstantOrSorceryCastThisTurnEffect copyEffect) {
        gameData.addPendingSpellCopy(playerId, new GameData.PendingSpellCopy(
                permanent != null ? permanent.getCard() : null, permanent != null ? permanent.getId() : null,
                copyEffect.dynamicCopyCount() == null ? 1 : 0, copyEffect.dynamicCopyCount(),
                copyEffect.maxManaValue() == null ? List.of() : List.of(copyEffect.maxManaValue()), 0, null));
        log.info("Game {} - {} will copy their next instant or sorcery spell this turn", gameData.id, playerId);
    }
}
