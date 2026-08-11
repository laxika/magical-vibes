package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromTriggeringSpellColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

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
        if (source == null || triggeringSpell == null) {
            return;
        }

        Set<CardColor> colors = Set.copyOf(triggeringSpell.getCard().getColors());
        if (colors.isEmpty()) {
            return;
        }

        Card copy = source.getCard().createRuntimeCopy();
        copy.addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(colors));
        copy.freeze();
        source.setCard(copy);
    }
}
