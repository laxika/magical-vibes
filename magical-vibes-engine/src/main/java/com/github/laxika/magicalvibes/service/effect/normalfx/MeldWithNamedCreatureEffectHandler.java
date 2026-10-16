package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link MeldWithNamedCreatureEffect}: exile the source and a matching named partner,
 * then put the source's back-face meld result onto the battlefield (CR 701.42a).
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
    private final PlayerInputService playerInputService;

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
                e.partnerPredicate(),
                new PermanentOwnedBySourceControllerPredicate()));

        List<Permanent> partners = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getId().equals(source.getId())) {
                    continue;
                }
                if (predicateEvaluationService.matchesPermanentPredicate(p, partnerFilter, filterContext)) {
                    partners.add(p);
                }
            }
        }
        if (partners.isEmpty()) {
            log.info("Game {} - meld skipped: no owned/controlled permanent named {}",
                    gameData.id, e.partnerName());
            return;
        }
        if (partners.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MeldPartnerChoice(
                    controllerId, source.getId(), e));
            playerInputService.beginPermanentChoice(gameData, controllerId,
                    partners.stream().map(Permanent::getId).toList(),
                    "Choose a creature to meld with " + source.getCard().getName() + ".");
            return;
        }
        meld(gameData, source, partners.getFirst(), controllerId, e);
    }

    /**
     * Completes a meld whose partner the controller chose from several eligible permanents.
     */
    public void completeMeldPartnerChoice(GameData gameData, UUID partnerId,
                                          PermanentChoiceContext.MeldPartnerChoice context) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null) {
            return;
        }
        Permanent partner = gameQueryService.findPermanentById(gameData, partnerId);
        if (partner == null) {
            throw new IllegalStateException("Chosen meld partner no longer exists");
        }
        meld(gameData, source, partner, context.controllerId(), context.effect());
    }

    private void meld(GameData gameData, Permanent source, Permanent partner, UUID controllerId,
                      MeldWithNamedCreatureEffect e) {
        // CR 701.42b/701.42c: tokens, and permanents that are only copies of a meld card (their
        // card is not the named partner), can't be melded. The exile still happens, a token ceases
        // to exist in exile, and the card that is not a token stays exiled.
        Card meldResultTemplate = source.getOriginalCard().getBackFaceCard();
        if (source.getCard().isToken() || partner.getCard().isToken()
                || meldResultTemplate == null
                || !e.partnerName().equals(partner.getOriginalCard().getName())) {
            permanentRemovalService.removePermanentToExile(gameData, source);
            permanentRemovalService.removePermanentToExile(gameData, partner);
            log.info("Game {} - meld skipped: the exiled objects can't be melded", gameData.id);
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
        // CR 202.3c: a melded permanent's mana value is the combined mana cost of its front faces.
        meldResult.setCombinedManaValue(sourceCard.getManaValue() + partnerCard.getManaValue());
        Permanent melded = new Permanent(meldResult);
        melded.setEnteredFromExile(true);
        melded.getMeldComponentCards().add(sourceCard);
        melded.getMeldComponentCards().add(partnerCard);
        if (e.entersTappedAndAttacking()) {
            melded.tap();
            melded.setAttacking(true);
            melded.setAttackTarget(source.getAttackTarget());
        }
        if (meldResult.hasType(CardType.PLANESWALKER) && meldResult.getLoyalty() != null) {
            melded.setCounterCount(CounterType.LOYALTY, meldResult.getLoyalty());
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, melded);
        // Melded permanent enters the battlefield (CR 701.42a) — fire its ETB triggers.
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
