package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSourceEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CloneService cloneService;
    private final AmountEvaluationService amountEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenCopyOfSourceEffect) effect;
        
                // Try to get the source card from the battlefield; if the source left (e.g. planeswalker
                // at 0 loyalty after paying cost), fall back to the card stored on the stack entry
                // (CR 608.2b: abilities resolve even if the source has left the zone).
                Card sourceCard;
                Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                Permanent sourceForRelativeValues = sourcePermanent != null
                        ? sourcePermanent : entry.getSourcePermanentSnapshot();
                if (sourcePermanent != null) {
                    sourceCard = sourcePermanent.getCard();
                } else {
                    sourceCard = entry.getCard();
                    if (sourceCard == null) {
                        log.info("Game {} - Source permanent no longer on battlefield and no card reference", gameData.id);
                        return;
                    }
                }

                int tokenMultiplier = gameQueryService.getTokenMultiplier(
                        gameData, entry.getControllerId(), sourceCard.hasType(CardType.CREATURE));
                int totalAmount = e.amount() * tokenMultiplier;
                for (int copy = 0; copy < totalAmount; copy++) {
                    // Create a token that's a copy of the source permanent (copying all copiable values per CR 707.2)
                    Card tokenCard = new Card();
                    tokenCard.setName(sourceCard.getName());
                    tokenCard.setType(sourceCard.getType());
                    EnumSet<CardType> additionalTypes = EnumSet.noneOf(CardType.class);
                    if (sourceCard.getAdditionalTypes() != null) {
                        additionalTypes.addAll(sourceCard.getAdditionalTypes());
                    }
                    if (e.additionalTypes() != null) {
                        e.additionalTypes().stream()
                                .filter(type -> type != sourceCard.getType())
                                .forEach(additionalTypes::add);
                    }
                    tokenCard.setAdditionalTypes(additionalTypes);
                    // Embalm / Eternalize copies have no mana cost.
                    tokenCard.setManaCost(!e.removeManaCost() && sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
                    tokenCard.setToken(true);
                    // Embalm / Eternalize recolors the copy (e.g. a white Zombie); otherwise keep the source's color.
                    if (e.colorOverride() != null) {
                        tokenCard.setColor(e.colorOverride());
                        tokenCard.setColors(List.of(e.colorOverride()));
                    } else {
                        tokenCard.setColor(sourceCard.getColor());
                    }
                    tokenCard.setLoyalty(sourceCard.getLoyalty());
                    // Embalm keeps the source's P/T; Eternalize sets a fixed base P/T (e.g. a 4/4).
                    tokenCard.setPower(e.powerOverride() != null ? e.powerOverride() : sourceCard.getPower());
                    tokenCard.setToughness(e.toughnessOverride() != null ? e.toughnessOverride() : sourceCard.getToughness());
                    // Embalm / Eternalize adds a creature type (e.g. Zombie) to the copy.
                    if (e.addedSubtype() != null && !sourceCard.getSubtypes().contains(e.addedSubtype())) {
                        List<CardSubtype> subtypes = new ArrayList<>(sourceCard.getSubtypes());
                        subtypes.add(e.addedSubtype());
                        tokenCard.setSubtypes(subtypes);
                    } else {
                        tokenCard.setSubtypes(sourceCard.getSubtypes());
                    }
                    tokenCard.setCardText(sourceCard.getCardText());
                    tokenCard.setSetCode(sourceCard.getSetCode());
                    tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

                    // Handle supertypes: optionally remove LEGENDARY
                    if (e.removeLegendary() && sourceCard.getSupertypes().contains(CardSupertype.LEGENDARY)) {
                        EnumSet<CardSupertype> modifiedSupertypes = EnumSet.copyOf(sourceCard.getSupertypes());
                        modifiedSupertypes.remove(CardSupertype.LEGENDARY);
                        tokenCard.setSupertypes(modifiedSupertypes);
                    } else {
                        tokenCard.setSupertypes(sourceCard.getSupertypes());
                    }

                    // Copy keywords and apply any plain-copy exception.
                    if ((sourceCard.getKeywords() != null && !sourceCard.getKeywords().isEmpty()) || e.grantHaste()) {
                        EnumSet<Keyword> keywords = sourceCard.getKeywords() == null
                                ? EnumSet.noneOf(Keyword.class)
                                : EnumSet.copyOf(sourceCard.getKeywords());
                        if (e.grantHaste()) {
                            keywords.add(Keyword.HASTE);
                        }
                        tokenCard.setKeywords(keywords);
                    }

                    // Copy effects and activated abilities (copiable characteristics per CR 707.2)
                    for (EffectSlot slot : EffectSlot.values()) {
                        for (EffectRegistration reg : sourceCard.getEffectRegistrations(slot)) {
                            tokenCard.addEffect(slot, reg.effect(), reg.triggerMode());
                        }
                    }
                    for (ActivatedAbility ability : sourceCard.getActivatedAbilities()) {
                        tokenCard.addActivatedAbility(ability);
                    }
                    tokenCard.copyTargetingFrom(sourceCard);

                    // Vizier of Many Faces: the embalm token is itself a Clone. Route it through the
                    // copy-on-enter replacement so it enters as a copy of a chosen creature; the clone
                    // flow (CloneService.completeCloneEntry) puts it onto the battlefield and re-applies
                    // the embalm color/type/no-mana-cost transformation to the final copy.
                    if (tokenCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                            .anyMatch(eff -> eff instanceof CopyPermanentOnEnterEffect)
                            && cloneService.prepareCloneReplacementEffect(gameData, entry.getControllerId(), tokenCard, null)) {
                        gameLogService.append(gameData, GameLog.textCardText(
                                "A token copy of ", sourceCard, " is created."));
                        log.info("Game {} - Token clone copy of {} created via embalm", gameData.id, sourceCard.getName());
                            return;
                    }

                    tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                            gameData, entry.getControllerId(), tokenCard);
                    Permanent tokenPermanent = new Permanent(tokenCard);

                    // Planeswalker tokens enter with loyalty counters and no summoning sickness
                    if (tokenCard.getType() == CardType.PLANESWALKER) {
                        tokenPermanent.setCounterCount(CounterType.LOYALTY, tokenCard.getLoyalty() != null ? tokenCard.getLoyalty() : 0);
                        tokenPermanent.setSummoningSick(false);
                    }

                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), tokenPermanent);
                    entry.getCreatedPermanentIds().add(tokenPermanent.getId());

                    if (e.tappedAndAttacking()) {
                        tokenPermanent.tap();
                        tokenPermanent.setAttacking(true);
                        if (sourceForRelativeValues != null) {
                            tokenPermanent.setAttackTarget(sourceForRelativeValues.getAttackTarget());
                        }
                    }

                    if (e.exileAtEndStep()) {
                        gameData.queueDelayedAction(new DelayedPermanentAction(
                                tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
                    }

                    if (e.removeLegendary()) {
                        gameLogService.append(gameData, GameLog.textCardText(
                                "A non-legendary token copy of ", sourceCard, " is created."));
                    } else {
                        gameLogService.append(gameData, GameLog.textCardText(
                                "A token copy of ", sourceCard, " is created."));
                    }
                    log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(), sourceCard.getName());

                    // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
                    // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, entry.getControllerId(), tokenCard, null, false);

                    if (e.initialCounters() != null && !e.initialCounters().isEmpty()
                            && !gameQueryService.cantHaveCounters(gameData, tokenPermanent)) {
                        AmountContext amountContext = AmountContext.forStackEntry(entry, sourceForRelativeValues);
                        for (var counterEntry : e.initialCounters().entrySet()) {
                            int count = amountEvaluationService.evaluate(
                                    gameData, counterEntry.getValue(), amountContext);
                            if (count > 0) {
                                permanentCounterSupport.placeCounterOnPermanent(
                                        gameData, entry, tokenPermanent, counterEntry.getKey(), count);
                            }
                        }
                    }
                }
    
    }
}
