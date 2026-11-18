package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/** Resolves a keyword grant to a creature selected by an earlier effect in the same resolution. */
@Component
@RequiredArgsConstructor
public class GrantKeywordToChosenCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToChosenCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantKeywordToChosenCreatureEffect grant = (GrantKeywordToChosenCreatureEffect) effect;
        UUID chosenCreatureId = grant.chosenCreatureId() != null
                ? grant.chosenCreatureId() : entry.getChosenPermanentId();
        if (chosenCreatureId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, chosenCreatureId);
        if (target == null || !gameQueryService.isCreature(gameData, target)
                || gameQueryService.cantHaveOrGainKeyword(gameData, target, grant.keyword())) {
            return;
        }

        Duration duration = grant.duration();
        if (!Duration.UNTIL_END_OF_TURN.equals(duration) && !Duration.UNTIL_YOUR_NEXT_TURN.equals(duration)) {
            throw new IllegalArgumentException("Unsupported chosen-creature keyword duration: " + duration);
        }

        Set<Keyword> keywords = Set.of(grant.keyword());
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new GrantKeywordEffect(keywords, GrantScope.TARGET, duration), target.getId(), null,
                null, duration, 0));

        gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                .text(" gains " + formatKeyword(grant.keyword()) + " " + durationLabel(duration) + ".")
                .build());
    }

    private String durationLabel(Duration duration) {
        return Duration.UNTIL_YOUR_NEXT_TURN.equals(duration)
                ? "until your next turn" : "until end of turn";
    }

    private String formatKeyword(Keyword keyword) {
        return keyword.name().charAt(0) + keyword.name().substring(1).toLowerCase().replace('_', ' ');
    }
}
