package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EachControlledLandOfChosenNonbasicTypeBecomesCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachControlledLandOfChosenNonbasicTypeBecomesCopyOfTargetCreatureUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachControlledLandOfChosenNonbasicTypeBecomesCopyOfTargetCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellNonbasicLandTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId);
        if (target == null || !predicateEvaluationService.matchesPermanentPredicate(
                target, targetPredicate, filterContext)) {
            log.info("Game {} - March from Velis Vel target is no longer legal", gameData.id);
            return;
        }

        PermanentPredicate affectedPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSubtypePredicate(chosenSubtype)));
        List<Permanent> affected = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, affectedPredicate, filterContext)) {
                affected.add(permanent);
            }
        }

        Card targetCard = target.getCard();
        List<Permanent> copies = affected.stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .toList();
        if (affected.stream().anyMatch(permanent -> permanent.getId().equals(target.getId()))) {
            copies = new ArrayList<>(copies);
            copies.add(target);
        }

        for (Permanent permanent : copies) {
            if (!permanent.isCopyUntilEndOfTurn()) {
                permanent.setPreCopyCard(permanent.getCard());
            }
            permanentCopierService.applyCloneCopy(permanent, targetCard, null, null, Set.of());
            EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(permanent.getCard().getKeywords());
            keywords.add(Keyword.HASTE);
            permanent.getCard().setKeywords(Set.copyOf(keywords));
            permanent.setCopyUntilEndOfTurn(true);
            gameData.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), permanent.getId(), controllerId,
                    effect, permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" makes " + copies.size() + " land(s) a hasty copy of "
                        + targetCard.getName() + " until end of turn.")
                .build());
    }
}
