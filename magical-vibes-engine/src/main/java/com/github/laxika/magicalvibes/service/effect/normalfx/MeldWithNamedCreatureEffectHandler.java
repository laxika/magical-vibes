package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link MeldWithNamedCreatureEffect}: exile the source and a matching named partner,
 * then put the source's back-face meld result onto the battlefield (CR 701.37).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MeldWithNamedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MeldWithNamedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MeldWithNamedCreatureEffect e = (MeldWithNamedCreatureEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(source.getOriginalCard().getId())
                .withSourceControllerId(controllerId);

        if (!predicateEvaluationService.matchesPermanentPredicate(
                source, new PermanentOwnedBySourceControllerPredicate(), filterContext)) {
            log.info("Game {} - meld skipped: controller does not own {}", gameData.id, source.getCard().getName());
            return;
        }

        PermanentPredicate partnerFilter = new PermanentAllOfPredicate(List.of(
                new PermanentNamedPredicate(e.partnerName()),
                new PermanentIsCreaturePredicate(),
                new PermanentOwnedBySourceControllerPredicate()));

        Permanent partner = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getId().equals(source.getId())) {
                    continue;
                }
                if (predicateEvaluationService.matchesPermanentPredicate(p, partnerFilter, filterContext)) {
                    partner = p;
                    break;
                }
            }
        }
        if (partner == null) {
            log.info("Game {} - meld skipped: no owned/controlled creature named {}",
                    gameData.id, e.partnerName());
            return;
        }

        Card meldResultTemplate = source.getOriginalCard().getBackFaceCard();
        if (meldResultTemplate == null) {
            log.warn("Game {} - {} has no meld result (back face)", gameData.id, source.getCard().getName());
            return;
        }

        Card sourceCard = source.getOriginalCard();
        Card partnerCard = partner.getOriginalCard();
        String sourceName = source.getCard().getName();
        String partnerNameLogged = partner.getCard().getName();

        permanentRemovalService.removePermanentToExile(gameData, source);
        permanentRemovalService.removePermanentToExile(gameData, partner);
        gameData.removeFromExile(sourceCard.getId());
        gameData.removeFromExile(partnerCard.getId());

        Card meldResult = instantiateMeldResult(meldResultTemplate, sourceCard.getSetCode());
        Permanent melded = new Permanent(meldResult);
        melded.getMeldComponentCards().add(sourceCard);
        melded.getMeldComponentCards().add(partnerCard);

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, melded);
        // Melded permanent enters the battlefield (CR 701.37c) — fire its ETB triggers.
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, controllerId, meldResult, null, false);

        gameLogService.append(gameData, GameLog.builder()
                .card(sourceCard).text(" and ").card(partnerCard)
                .text(" meld into ").card(meldResult).text(".")
                .build());
        log.info("Game {} - {} and {} meld into {}",
                gameData.id, sourceName, partnerNameLogged, meldResult.getName());
    }

    /**
     * Builds the meld result and gives it the printing identity the client draws it from.
     *
     * <p>The melding halves' own printing is not it: a meld result is a separate card with its own
     * collector number, so Gisela's INR 24 would fetch Gisela's art for Brisela. The set is the
     * melding card's — a meld result is only ever printed alongside its halves — and the collector
     * number comes from the meld result class's own registration in that set. A set that prints
     * the halves without the result leaves the number unset, exactly as before.
     */
    private static Card instantiateMeldResult(Card template, String setCode) {
        Class<? extends Card> meldResultClass = template.getClass();
        Card meldResult;
        try {
            meldResult = meldResultClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Failed to instantiate meld result " + meldResultClass.getSimpleName(), ex);
        }
        if (setCode != null) {
            meldResult.setSetCode(setCode);
            CardScanner.collectorNumberOf(meldResultClass, setCode)
                    .ifPresent(meldResult::setCollectorNumber);
        }
        return meldResult;
    }
}
