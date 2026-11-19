package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDamageAtNextUpkeepUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Quenchable Fire's SPELL registrar: reads the spell's target (player or planeswalker) from
 * the stack entry's {@code targetId} and adds a delayed triggered ability
 * for the spell controller's next upkeep.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDamageAtNextUpkeepUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDamageAtNextUpkeepUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDamageAtNextUpkeepUnlessPaysEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        UUID spellControllerId = entry.getControllerId();
        // "That player or that planeswalker" is the one the spell targeted, which its delayed ability names rather than
        // targets again (CR 115.10a).
        boolean targetsPlayer = gameData.playerIds.contains(targetId);
        CardEffect damage = targetsPlayer
                ? new DealDamageToPlayersEffect(e.damage(), Subjects.triggeringPlayer())
                : new DealDamageToTargetCreatureEffect(Subjects.triggering(), e.damage());
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStepUnlessPaid(
                spellControllerId, entry.getCard(), new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), damage),
                entry.getSourcePermanentId(), targetsPlayer ? null : targetId, targetsPlayer ? targetId : null,
                e.manaCost()));

        gameLogService.append(gameData, GameLog.text(entry.getCard().getName()
                + " will deal an additional " + e.damage() + " damage at the beginning of "
                + gameData.playerIdToName.get(spellControllerId) + "'s next upkeep unless " + e.manaCost()
                + " is paid first."));
        log.info("Game {} - {} scheduled an upkeep pay-or-take-{}-damage obligation for {}",
                gameData.id, entry.getCard().getName(), e.damage(), gameData.playerIdToName.get(spellControllerId));
    }
}
