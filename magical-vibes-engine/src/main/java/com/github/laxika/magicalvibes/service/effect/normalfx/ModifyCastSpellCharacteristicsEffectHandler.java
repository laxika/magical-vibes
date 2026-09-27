package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ModifyCastSpellCharacteristicsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Carries characteristic changes from a resolving spell-cast trigger onto its permanent spell. */
@Component
@RequiredArgsConstructor
public class ModifyCastSpellCharacteristicsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ModifyCastSpellCharacteristicsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null) {
            return;
        }
        StackEntry spellEntry = gameQueryService.findStackEntryByCardId(gameData, spellCardId);
        if (spellEntry == null) {
            return;
        }

        ModifyCastSpellCharacteristicsEffect change = (ModifyCastSpellCharacteristicsEffect) effect;
        if (change.additionalColor() != null) {
            spellEntry.getGrantedColorsOnEntry().add(change.additionalColor());
        }
        if (change.additionalSubtype() != null) {
            spellEntry.getGrantedSubtypesOnEntry().add(change.additionalSubtype());
        }
        if (change.basePower() != null) {
            spellEntry.setBasePowerOverrideOnEntry(change.basePower());
        }
        if (change.baseToughness() != null) {
            spellEntry.setBaseToughnessOverrideOnEntry(change.baseToughness());
        }
    }
}
