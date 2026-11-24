package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.DelayedExiledCardReturns;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final CreateTokenCopyOfChosenCreatureEffectHandler chosenCreatureTokenCopyHandler;
    private final EsixTokenReplacementSupport esixTokenReplacementSupport;
    private final ChooseAttackTargetsForCreatedTokensEffectHandler attackTargetsHandler;
    private final SubjectResolutionService subjectResolutionService;

    public CreateTokenEffectHandler(PermanentControlSupport permanentControlSupport,
                                    GameQueryService gameQueryService,
                                    AmountEvaluationService amountEvaluationService,
                                    CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler,
                                    CreateTokenCopyOfChosenCreatureEffectHandler chosenCreatureTokenCopyHandler,
                                    EsixTokenReplacementSupport esixTokenReplacementSupport,
                                    ChooseAttackTargetsForCreatedTokensEffectHandler attackTargetsHandler,
                                    SubjectResolutionService subjectResolutionService) {
        this.permanentControlSupport = permanentControlSupport;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.tokenCopyHandler = tokenCopyHandler;
        this.chosenCreatureTokenCopyHandler = chosenCreatureTokenCopyHandler;
        this.esixTokenReplacementSupport = esixTokenReplacementSupport;
        this.attackTargetsHandler = attackTargetsHandler;
        this.subjectResolutionService = subjectResolutionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();
        // "Its controller creates": the player the blueprint's token controller names, read again when a token-creation
        // replacement's answer resolves this effect once more.
        List<UUID> creators = subjectResolutionService.players(gameData, entry, e, e.controller());
        int first = 0;
        // With several creators ("each player who controls the most creatures investigates") the answer resumes at the
        // player the offer was made to, so those before them don't create again.
        if (creators.size() > 1 && gameData.pendingTokenCreationReplacement != null) {
            first = Math.max(0, creators.indexOf(gameQueryService.findPermanentController(
                    gameData, gameData.pendingTokenCreationReplacement.replacementPermanentId())));
        }
        for (UUID creatorId : creators.subList(first, creators.size())) {
            resolveForController(gameData, entry, e, creatorId);
            if (gameData.pendingTokenCreationReplacement != null) {
                break;
            }
        }
        recordCreated(gameData, entry, createdBefore);
    }

    /**
     * The step's result: the tokens it created (those {@code entry} recorded after the first {@code createdBefore}),
     * whoever made them, and their controllers. A creation that waits for a replacement choice records none and is
     * resolved again once the choice is made.
     */
    private void recordCreated(GameData gameData, StackEntry entry, int createdBefore) {
        List<UUID> created = List.copyOf(entry.getCreatedPermanentIds()
                .subList(Math.min(createdBefore, entry.getCreatedPermanentIds().size()),
                        entry.getCreatedPermanentIds().size()));
        List<UUID> controllers = new ArrayList<>();
        for (UUID tokenId : created) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, tokenId);
            if (controllerId != null) {
                controllers.add(controllerId);
            }
        }
        entry.recordStep(StepKind.CREATE_TOKEN, StepOutcome.ofObjects(created, controllers));
    }

    public void resolveForController(GameData gameData, StackEntry entry, CreateTokenEffect e,
                                     UUID controllerId) {
        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source).withControllerId(controllerId);
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), context);
        int primaryAmount = amount;
        for (CreateTokenEffect simultaneousToken : e.simultaneousTokens()) {
            amount += amountEvaluationService.evaluate(gameData, simultaneousToken.amount(), context);
        }
        if (amount <= 0) {
            return;
        }
        // "Investigate N times" is N separate creation events, so a "first time you would create
        // tokens" replacement (Esix, Mirrormind Crown, Moonlit Meditation) applies to the first
        // investigation only; the rest resolve as their own effect afterwards.
        if (e.investigate() && amount > 1 && e.simultaneousTokens().isEmpty()
                && gameData.pendingTokenCreationReplacement == null
                && entry.getResolvingEffectIndex() >= 0
                && hasAvailableTokenCreationReplacement(gameData, controllerId)) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(e.withAmount(amount - 1)));
            amount = 1;
            primaryAmount = 1;
        }
        if (e.investigate() && gameData.pendingTokenCreationReplacement == null) {
            for (int action = 0; action < amount; action++) {
                ActionEvents.investigated(gameData, controllerId);
            }
        }
        int power = amountEvaluationService.evaluate(gameData, e.power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.toughness(), context);

        PendingTokenCreationReplacement pending = gameData.pendingTokenCreationReplacement;
        if (pending != null) {
            gameData.pendingTokenCreationReplacement = null;
            Boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (Boolean.TRUE.equals(accepted)) {
                // The player who would have created the tokens creates the copies instead.
                if (pending.copyChosenCreature()) {
                    chosenCreatureTokenCopyHandler.resolveFor(gameData, entry,
                            new CreateTokenCopyOfChosenCreatureEffect(
                                    pending.amount(), pending.replacementPermanentId()), controllerId);
                } else {
                    int createdBefore = entry.getCreatedPermanentIds().size();
                    tokenCopyHandler.createCopiesOfHost(gameData, entry, pending.replacementPermanentId(),
                            pending.amount(), controllerId);
                    if (e.tapped()) {
                        // The replacement changes what is created, not how the original event had it enter.
                        entry.getCreatedPermanentIds().stream().skip(createdBefore)
                                .map(id -> gameQueryService.findPermanentById(gameData, id))
                                .filter(java.util.Objects::nonNull)
                                .forEach(Permanent::tap);
                    }
                }
                return;
            }
            if (Boolean.FALSE.equals(accepted)) {
                amount = pending.amount();
                power = pending.power();
                toughness = pending.toughness();
            }
        } else {
            if (esixTokenReplacementSupport.offerAttachedCopy(gameData, controllerId, amount, power, toughness)) {
                return;
            }
            if (esixTokenReplacementSupport.offer(gameData, controllerId, amount, power, toughness)) {
                return;
            }
        }

        if (!e.simultaneousTokens().isEmpty()) amount = primaryAmount;

        int createdFromIndex = entry.getCreatedPermanentIds().size();
        CardEffect deathReturn = deathReturn(e, entry);
        // That card as it is exiled now, with the ability that exiled it as a cost resolving (CR 400.7j, 603.7c).
        var deathReturnReferents = deathReturn == null ? null
                : DelayedExiledCardReturns.exiled(gameData, List.of(entry.getCard().getId()));
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(gameData, controllerId,
                deathReturn == null ? e : withoutDeathReturn(e), amount, entry.getCard().getSetCode(), power,
                toughness);
        if (deathReturn != null) {
            // "Return [this] to the battlefield when that token dies" (Tatsumasa, the Dragon's Fang): a delayed triggered
            // ability about the token, with no stated duration, controlled by the resolving ability's controller
            // (CR 603.7a, 603.7b, 603.7e).
            for (UUID createdId : createdIds) {
                if (gameQueryService.findPermanentById(gameData, createdId) != null) {
                    gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                            entry.getControllerId(), entry.getCard(),
                            new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), deathReturn), createdId,
                            entry.getSourcePermanentId(), false).withReferents(deathReturnReferents));
                }
            }
        }
        // The tokens' controller may differ from the one that created them (Crafty Cutpurse); only the active
        // player's tokens attack, and their controller chooses what each one attacks once creation has finished (CR 508.4).
        UUID tokenController = createdIds.isEmpty()
                ? controllerId : gameQueryService.findPermanentController(gameData, createdIds.getFirst());
        if (e.tappedAndAttacking() && tokenController.equals(gameData.activePlayerId)
                && gameData.currentStep != null && gameData.currentStep.isCombatPhase()) {
            attackTargetsHandler.createdTokensAttack(gameData, entry, tokenController, createdFromIndex, false);
        }
        entry.getCreatedPermanentIds().addAll(createdIds);
    }

    private boolean hasAvailableTokenCreationReplacement(GameData gameData, UUID controllerId) {
        return esixTokenReplacementSupport.isAttachedCopyAvailable(gameData, controllerId)
                || esixTokenReplacementSupport.isAvailable(gameData, controllerId);
    }

    /**
     * The delayed "return this card from exile when that token dies" trigger a blueprint authors as a death
     * ability over {@code cardsThisWay(EXILE)} (Tatsumasa, the Dragon's Fang, exiled to pay its own activation
     * cost), bound to the card that created the token, or {@code null} when it has none. It is a delayed trigger
     * (CR 603.7), not an ability of the token, so it still triggers while the token has lost its abilities.
     */
    private static CardEffect deathReturn(CreateTokenEffect token, StackEntry entry) {
        if (token.tokenEffects() == null || entry.getCard() == null) {
            return null;
        }
        return token.tokenEffects().stream()
                .filter(CreateTokenEffectHandler::isDeathReturnPlaceholder)
                .findFirst()
                .map(ability -> AbilityEffects.effectsOf(ability).getFirst())
                .orElse(null);
    }

    private static CreateTokenEffect withoutDeathReturn(CreateTokenEffect token) {
        return token.withTokenEffects(token.tokenEffects().stream()
                .filter(ability -> !isDeathReturnPlaceholder(ability))
                .toList());
    }

    private static boolean isDeathReturnPlaceholder(Ability ability) {
        return ability instanceof TriggeredAbility triggered && triggered.hasTrigger(Triggers.dies(ObjectRelation.THIS))
                && AbilityEffects.effectsOf(ability).getFirst()
                instanceof PutCardsOntoBattlefieldEffect put
                // A token's own dies ability has no earlier step to read: this names what the maker exiled.
                && put.cards() instanceof CardSubject.CardsThisWay thisWay && thisWay.kind() == StepKind.EXILE;
    }
}
