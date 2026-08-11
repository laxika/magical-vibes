package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetSpellDamagePreventionShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromTargetSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventDamageFromTargetSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageFromTargetSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PreventDamageFromTargetSpellEffect e = (PreventDamageFromTargetSpellEffect) effect;
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (targetSpell == null || !isSpell(targetSpell.getEntryType())
                || (e.instantSorceryOnly()
                && targetSpell.getEntryType() != StackEntryType.INSTANT_SPELL
                && targetSpell.getEntryType() != StackEntryType.SORCERY_SPELL)) {
            return;
        }

        gameData.targetSpellDamagePreventionShields.add(new TargetSpellDamagePreventionShield(
                targetSpell.getCard().getId(), e.gainLife() ? entry.getControllerId() : null));
        gameLogService.append(gameData, GameLog.cardThen(targetSpell.getCard(), "'s damage is prevented this turn."));
    }

    private boolean isSpell(StackEntryType entryType) {
        return switch (entryType) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
    }
}
