package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DuelistsConvocationInternationalTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves Duelists' Convocation International's "cross out one of those digits and draw a card. Then if all ten digits
 * are crossed out, you win the game", reading the mana value of the spell or land that triggered it from the stack entry.
 */
@Component
@RequiredArgsConstructor
public class DuelistsConvocationInternationalTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DuelistsConvocationInternationalTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || source.getChosenNumberDigits().size() != 10) {
            return;
        }

        // "It": the triggering spell as it was on the stack (X the number chosen for it, CR 202.3e; 0 cast face down),
        // else the land played.
        StackEntry spell = entry.getTriggeringSpellSnapshot();
        Card triggeringCard = entry.triggeringSpellOrCard();
        if (spell == null && triggeringCard == null) {
            return;
        }
        int manaValue = spell != null ? (spell.isCastFaceDown() ? 0 : spell.getSpellManaValue())
                : triggeringCard.getManaValue();
        int crossedPosition = source.uncrossedChosenNumberDigitPosition(manaValue);
        if (crossedPosition < 0) {
            return;
        }

        source.getCrossedNumberDigitPositions().add(crossedPosition);
        // After the effect being resolved, which the ability's intervening "if" wraps.
        int effectIndex = entry.getResolvingEffectIndex();
        if (effectIndex < 0) {
            return;
        }

        List<CardEffect> followUps = new ArrayList<>();
        followUps.add(new DrawCardEffect(1));
        if (source.getCrossedNumberDigitPositions().size() == 10) {
            followUps.add(new WinGameEffect());
        }
        entry.insertEffectsToResolve(effectIndex + 1, followUps);
    }
}
