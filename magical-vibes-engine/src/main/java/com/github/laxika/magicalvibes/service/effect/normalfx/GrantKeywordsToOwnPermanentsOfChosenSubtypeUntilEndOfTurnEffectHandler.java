package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToOwnPermanentsOfChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantKeywordsToOwnPermanentsOfChosenSubtypeUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordsToOwnPermanentsOfChosenSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantKeywordsToOwnPermanentsOfChosenSubtypeUntilEndOfTurnEffect) effect;
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, entry.getControllerId());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        PermanentHasSubtypePredicate subtypePredicate = new PermanentHasSubtypePredicate(chosenSubtype);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(sourcePermanentId)
                        || !predicateEvaluationService.matchesPermanentPredicate(
                        permanent, subtypePredicate, filterContext)) {
                    continue;
                }

                permanent.getGrantedKeywords().addAll(grant.keywords());
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), entry.getCard().getName(), sourcePermanentId,
                        entry.getControllerId(),
                        new GrantKeywordEffect(grant.keywords(), GrantScope.TARGET),
                        permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
                count++;
            }
        }

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" gives " + formatKeywords(grant.keywords()) + " to " + count
                        + " other permanent(s) until end of turn.").build());
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.name().charAt(0)
                        + keyword.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((first, second) -> first + ", " + second)
                .orElse("");
    }
}
