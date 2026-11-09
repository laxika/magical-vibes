package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityFromSnapshotEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CopyTriggeringSpellOrAbilityEffect}: an activation trigger's entry copies its triggering activated
 * ability as {@link CopyControllerActivatedAbilityEffectHandler} does, for this ability's controller; any other entry
 * copies its triggering spell as {@link CopyTriggeringSpellEffectHandler} does.
 */
@Component
@RequiredArgsConstructor
public class CopyTriggeringSpellOrAbilityEffectHandler implements NormalEffectHandlerBean {

    private final CopyTriggeringSpellEffectHandler copyTriggeringSpellEffectHandler;
    private final CopyControllerActivatedAbilityEffectHandler copyControllerActivatedAbilityEffectHandler;
    private final CopyTriggeredAbilityFromSnapshotEffectHandler copyTriggeredAbilityFromSnapshotEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTriggeringSpellOrAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTriggeringActivatedAbility() != null) {
            StackEntry ability = entry.getTriggeringSpellSnapshot();
            copyControllerActivatedAbilityEffectHandler.resolve(gameData, entry,
                    new CopyControllerActivatedAbilityEffect(ability == null ? null : new StackEntry(ability),
                            entry.getTriggeringActivatedAbility(), entry.getControllerId()));
            return;
        }
        StackEntry triggering = entry.getTriggeringSpellSnapshot();
        if (triggering != null && triggering.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            // "Copy that ability" of a triggered ability (Firebender Ascension): the ability as it triggered, copied
            // even if it has left the stack.
            copyTriggeredAbilityFromSnapshotEffectHandler.resolve(gameData, entry,
                    new CopyTriggeredAbilityFromSnapshotEffect(new StackEntry(triggering)));
            return;
        }
        copyTriggeringSpellEffectHandler.resolve(gameData, entry, new CopyTriggeringSpellEffect());
    }
}
