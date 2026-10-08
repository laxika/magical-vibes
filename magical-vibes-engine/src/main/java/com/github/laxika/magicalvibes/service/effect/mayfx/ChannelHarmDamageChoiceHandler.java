package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChannelHarmEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Channel Harm's optional damage as part of its prevention effect, without a new stack ability. */
@Component
@RequiredArgsConstructor
public class ChannelHarmDamageChoiceHandler implements MayEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChannelHarmEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ChannelHarmEffect damage = ability.effects().stream()
                .filter(ChannelHarmEffect.class::isInstance).map(ChannelHarmEffect.class::cast)
                .findFirst().orElseThrow();
        if (accepted && damage.preventedDamage() != null
                && gameQueryService.findPermanentById(gameData, ability.targetCardId()) != null) {
            StackEntry damageContext = new StackEntry(StackEntryType.INSTANT_SPELL,
                    ability.sourceCard(), ability.controllerId(), ability.description(),
                    List.of());
            damageContext.setTargetId(ability.targetCardId());
            damageContext.setNonTargeting(true);
            damageSupport.resolveAnyTargetDamage(gameData, damageContext,
                    ability.targetCardId(), damage.preventedDamage(), false);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
