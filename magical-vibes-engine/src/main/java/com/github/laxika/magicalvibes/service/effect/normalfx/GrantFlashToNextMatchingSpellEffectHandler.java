package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Floats {@link GrantFlashToNextMatchingSpellEffect} on the controller until a matching spell is cast or the turn ends,
 * with the chosen values of its filter written in as it resolves ("the next spell of the chosen type": the source's
 * choice as it is or last was, CR 608.2h).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrantFlashToNextMatchingSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlashToNextMatchingSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantFlashToNextMatchingSpellEffect) effect;
        CardPredicate predicate = subjectResolutionService.bindChosenValues(gameData, entry, effect, grant.predicate());
        gameData.addPlayerEffect(entry, new GrantFlashToNextMatchingSpellEffect(predicate),
                entry.getControllerId(), Duration.UNTIL_MATCHING_SPELL_CAST);
        String spell = spellDescription(predicate);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" lets its controller cast their next " + spell + " this turn as though it had flash.")
                .build());
        log.info("Game {} - {} grants flash to the next {} for player {} this turn",
                gameData.id, entry.getCard().getName(), spell, entry.getControllerId());
    }

    private static String spellDescription(CardPredicate predicate) {
        return switch (predicate) {
            case CardTruePredicate ignored -> "spell";
            case CardTypePredicate type -> type.cardType().name().toLowerCase(Locale.ROOT) + " spell";
            case CardSubtypePredicate subtype -> subtype.subtype().getDisplayName() + " spell";
            default -> "matching spell";
        };
    }
}
