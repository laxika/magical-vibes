package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Component
public class CopyNextSpellCastThisTurnManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextSpellCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        CopyNextSpellCastThisTurnEffect copyEffect = (CopyNextSpellCastThisTurnEffect) effect;
        if (copyEffect.spellFilter() == null && copyEffect.removedSupertypes().isEmpty()) {
            gameData.pendingNextSpellCopyThisTurnCount.merge(playerId, 1, Integer::sum);
        } else {
            gameData.pendingNextFilteredSpellCopiesThisTurn
                    .computeIfAbsent(playerId, ignored -> new ArrayList<>())
                    .add(copyEffect);
        }
        log.info("Game {} - {} will copy their next matching spell this turn", gameData.id, playerId);
    }
}
