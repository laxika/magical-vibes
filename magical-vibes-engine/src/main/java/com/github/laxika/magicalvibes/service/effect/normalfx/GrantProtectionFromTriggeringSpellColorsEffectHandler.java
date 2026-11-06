package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromTriggeringSpellColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantProtectionFromTriggeringSpellColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromTriggeringSpellColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        StackEntry triggeringSpell = gameQueryService.findStackEntryByCardId(
                gameData, entry.getTriggeringCardId());
        if (source == null) {
            return;
        }

        Set<CardColor> colors = triggeringSpell != null
                ? gameQueryService.getEffectiveCardColors(gameData, triggeringSpell.getCard())
                : lastKnownSpellColors(gameData, entry);
        if (colors == null) {
            colors = Set.of();
        }
        if (colors.isEmpty()) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), source.getId(), entry.getControllerId(),
                new ProtectionFromColorsEffect(colors), source.getId(), null, null,
                EffectDuration.PERMANENT, 0));
    }

    /**
     * Colors of a spell that has already left the stack (e.g. countered): its last known information
     * (CR 608.2h), which is the color it most recently had before leaving, falling back to the colors
     * of the spell as it was when the ability triggered (the stack entry's triggering spell).
     */
    private Set<CardColor> lastKnownSpellColors(GameData gameData, StackEntry entry) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId != null) {
            Set<CardColor> temporary = gameData.spellColorOverridesUntilEndOfTurn.get(triggeringCardId);
            if (temporary != null) {
                return temporary;
            }
            Set<CardColor> indefinite = gameData.spellColorOverrides.get(triggeringCardId);
            if (indefinite != null) {
                return indefinite;
            }
        }
        StackEntry spell = entry.getTriggeringSpellSnapshot();
        return spell == null ? null : Set.copyOf(spell.getCard().getColors());
    }
}
