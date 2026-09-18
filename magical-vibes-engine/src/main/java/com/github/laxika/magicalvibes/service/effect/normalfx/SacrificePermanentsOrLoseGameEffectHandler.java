package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrLoseGameEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves an exact-count sacrifice with a game-loss fallback when the count is impossible. */
@Component
@RequiredArgsConstructor
public class SacrificePermanentsOrLoseGameEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentsOrLoseGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrifice = (SacrificePermanentsOrLoseGameEffect) effect;
        UUID playerId = entry.getControllerId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        Permanent source = resolveSourcePermanent(gameData, entry);
        int count = amountEvaluationService.evaluate(gameData, sacrifice.count(),
                AmountContext.forStackEntry(entry, source));
        if (count <= 0) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(source != null && source.getOriginalCard() != null
                        ? source.getOriginalCard().getId()
                        : entry.getCard().getId())
                .withSourceControllerId(playerId)
                .withSourcePermanentSnapshot(source);
        List<Permanent> matching = battlefield == null ? List.of() : battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, sacrifice.filter(), filterContext))
                .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                .toList();

        if (matching.size() > count) {
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    playerId,
                    matching.stream().map(Permanent::getId).toList(),
                    count,
                    new MultiPermanentChoiceContext.ForcedSacrifice(playerId, List.of(), List.of()),
                    "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to sacrifice.");
            return;
        }

        if (!matching.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(
                    gameData, matching.stream().map(Permanent::getId).toList());
        }
        if (matching.size() < count) {
            int effectIndex = entry.getEffectsToResolve().indexOf(effect);
            if (effectIndex < 0) {
                throw new IllegalStateException(
                        "SacrificePermanentsOrLoseGameEffect is not in its stack entry");
            }
            entry.insertEffectsToResolve(effectIndex + 1, List.of(new ControllerLosesGameEffect()));
        }
    }

    private Permanent resolveSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return source;
    }
}
