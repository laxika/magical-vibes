package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.library.LibrarySearchRestrictions;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledLandsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final DrawService drawService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final com.github.laxika.magicalvibes.service.CardRevealService cardRevealService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private AuraAttachmentService auraAttachmentService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MayEffect) effect;
        CardEffect declineBranch = e.elseEffect() instanceof MayEffect elseMay ? elseMay.wrapped() : e.elseEffect();
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect
                && e.elseEffect() == null
                && gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).isEmpty()) {
            // Nothing can be discarded, so there is no choice to offer and "if you do" can't happen.
            return;
        }
        if (e.wrapped() instanceof SequenceEffect reveal && reveal.steps().size() > 1
                && reveal.steps().getFirst() instanceof ChooseCardEffect choice && !choice.upTo()
                && choice.cards().inHiddenZone()
                && reveal.steps().get(1) instanceof RevealCardsFromHandEffect shown && shown.cards() != null
                && subjectResolutionService.cards(gameData, entry, choice, choice.cards()).isEmpty()) {
            // No card of the kind in hand to reveal, so no choice is offered: revealing one didn't happen.
            insertElseEffect(entry, e);
            return;
        }
        if (e.wrapped() instanceof LookAtTopCardsOfTargetLibraryEffect look
                && look.action() == TargetLibraryAction.MAY_PUT_TOP_ON_BOTTOM
                && entry.getTargetId() != null
                && gameData.playerDecks.getOrDefault(entry.getTargetId(), List.of()).isEmpty()) {
            // The targeted library is empty, so there is no top card to look at and no choice is offered.
            return;
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.ExchangeControlOfPermanentAndSpellEffect exchange) {
            // Both objects are already controlled by the same player, so the exchange does nothing and no choice is offered.
            List<UUID> exchangedIds = subjectResolutionService.permanentIds(gameData, entry, exchange, exchange.permanent());
            StackEntry exchangeSpell = subjectResolutionService.spellOnStack(gameData, entry, exchange.spell());
            if (!exchangedIds.isEmpty() && exchangeSpell != null
                    && java.util.Objects.equals(
                    gameQueryService.findPermanentController(gameData, exchangedIds.getFirst()),
                    exchangeSpell.getControllerId())) {
                return;
            }
        }
        CardEffect leadingEffect = e.wrapped() instanceof SequenceEffect leadingSequence && !leadingSequence.steps().isEmpty()
                ? leadingSequence.steps().getFirst() : e.wrapped();
        boolean drawThenDiscard = e.wrapped() instanceof SequenceEffect drawSequence
                && drawSequence.steps().stream().anyMatch(com.github.laxika.magicalvibes.model.effect.DiscardEffect.class::isInstance);
        if (leadingEffect instanceof DrawCardEffect leadingDraw && leadingDraw.player() instanceof PlayerSubject.You
                && !drawThenDiscard
                && drawService.isDrawPrevented(gameData, entry.getControllerId())) {
            // The controller can't draw (e.g. Narset's draw limit), so the "may have you draw" choice
            // can't be carried out and no choice is offered; the "if they do" branch never happens.
            // A "draw a card, then discard a card" loot is still offered: the discard is a separate
            // instruction that is followed even though the draw is impossible (CR 608.2c, CR 609.3).
            insertElseEffect(entry, e);
            return;
        }
        if (e.wrapped() instanceof MillControllerAndMayPutMilledLandsOntoBattlefieldEffect mill
                && e.elseEffect() == null
                && amountEvaluationService.evaluate(gameData, mill.count(),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot()))
                > gameData.playerDecks.getOrDefault(entry.getControllerId(), List.of()).size()) {
            // CR 701.17b — a player can't choose to mill more cards than their library holds, so no choice is offered.
            return;
        }
        if (e.wrapped() instanceof ReturnCardFromGraveyardEffect battlefieldReturn
                && battlefieldReturn.targetGraveyard()
                && battlefieldReturn.destination() == GraveyardChoiceDestination.BATTLEFIELD
                && declineBranch instanceof ReturnCardFromGraveyardEffect handReturn
                && handReturn.destination() == GraveyardChoiceDestination.HAND) {
            UUID graveyardTargetId = entry.getTargetId() != null ? entry.getTargetId()
                    : entry.getTargetCardIds().isEmpty() ? null : entry.getTargetCardIds().getFirst();
            Card card = gameQueryService.findCardInGraveyardById(gameData, graveyardTargetId);
            if (card != null && (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)
                    || card.isAura() && !hasLegalAuraAttachment(gameData, card, entry.getControllerId()))) {
                insertElseEffect(entry, e);
                return;
            }
        }
        if (leadingEffect instanceof ExileGraveyardCardsEffect ownExile
                && ownExile.scope() == GraveyardExileScope.OWN
                && !hasOwnGraveyardCardsToExile(gameData, entry, ownExile)) {
            // "You may exile [N] cards from your graveyard": with fewer such cards there is no choice to offer, so
            // nothing is exiled and its "if you do" / "when you do" never happens.
            insertElseEffect(entry, e);
            return;
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.BlightEffect
                && !controlsBlightableCreature(gameData, entry.getControllerId())) {
            insertElseEffect(entry, e);
            return;
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect tap
                && tap.subject() instanceof com.github.laxika.magicalvibes.model.effect.PermanentSubject.Triggering) {
            var permanent = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
            if (permanent == null || permanent.isTapped()) {
                insertElseEffect(entry, e);
                return;
            }
        }
        if (picksNameNothing(gameData, entry, leadingEffect)) {
            // "If it's a double-faced card, you may transform it", "if an Equipment is put onto the battlefield this
            // way, you may attach it": the resolution's picks include no such permanent, so there is no choice.
            insertElseEffect(entry, e);
            return;
        }
        CardEffect firstEffect = e.wrapped() instanceof SequenceEffect sequence && !sequence.steps().isEmpty()
                ? sequence.steps().getFirst() : e.wrapped();
        if (firstEffect instanceof PutCountersEffect counters
                && counters.predicate() == null
                && counters.subject() instanceof com.github.laxika.magicalvibes.model.effect.PermanentSubject.Target) {
            var target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null || !canHaveCounters(gameData, target, counters.counterType())) {
                insertElseEffect(entry, e);
                return;
            }
        }
        if (e.wrapped() instanceof PutCountersEffect counters
                && counters.subject() instanceof com.github.laxika.magicalvibes.model.effect.PermanentSubject.Source) {
            // "You may put a counter on this": no choice when the source can't have the counters, or is gone and an
            // "if you don't" follows (CR 608.2d). Gone with nothing else to do, the controller still makes it (Kresh's
            // trigger after Kresh left the battlefield).
            var source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source == null && e.elseEffect() == null) {
                // Nothing replaces the counters and nothing observable depends on the choice.
            } else if (source == null
                    || !canHaveCounters(gameData, source, counters.counterType())) {
                insertElseEffect(entry, e);
                return;
            }
        }

        // CR 702.99a — cipher is "If this spell is represented by a card, you may exile this card
        // encoded on a creature you control". A copy cast off the encoded card is not represented by
        // a card, so the copy's cipher ability does nothing and must not prompt.
        if (entry.isCopy() && e.wrapped() instanceof CipherEncodeEffect) {
            return;
        }

        // On a multi-target card each "you may" is bound to its own target group, so the pending
        // ability must carry that group's target rather than the entry's lone one. A bound group
        // with no legal target left does nothing (CR 608.2b) — don't even prompt.
        UUID targetId = entry.getTargetId();
        List<UUID> groupTargets = entry.targetsForBoundEffectGroup(e);
        if (groupTargets != null && !entry.getTargetIds().isEmpty()) {
            if (groupTargets.isEmpty()) {
                return;
            }
            targetId = groupTargets.getFirst();
        }

        // An effect that acts only on its targets does nothing when its "up to N" / "any number of"
        // target choice took none (CR 601.2c, 603.3d): no permanent or player and no graveyard card was
        // chosen, so its "you may" has no choice to offer.
        if (e.wrapped().actsOnlyOnItsTargets() && targetId == null
                && entry.getTargetIds().isEmpty() && entry.getTargetCardIds().isEmpty()) {
            return;
        }

        // An effect that picks its graveyard cards as the ability goes on the stack (CR 603.3d) acts only on them, so
        // when that choice took none it does nothing and its "you may" has no choice to offer (Rysorian Badger, Zombie
        // Cannibal, Spellweaver Helix with no card to choose).
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect choosing
                && choosing.choosesGraveyardCards()
                && entry.getTargetCardIds().isEmpty() && entry.getTargetIds().isEmpty()) {
            return;
        }

        // Optional hand-ability targets can be omitted (e.g. Decree of Silence cycling).
        // Skip that optional effect while continuing the ability's remaining effects.
        if (targetId == null && entry.isCyclingAbility()
                && e.wrapped() instanceof CounterSpellEffect) {
            return;
        }

        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect hand
                && hand.looksAtHandBeforeMayChoice() && targetId != null && gameData.playerIds.contains(targetId)
                && e.choicePlayer() == MayChoicePlayer.CONTROLLER) {
            cardRevealService.lookAtHand(gameData, entry.getControllerId(), targetId);
        }

        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect retarget) {
            // "May choose new targets for [spell]": the offer names the spell its subject names while it is on the
            // stack; the retarget flow says so when there is nothing to change. After a control change nothing is
            // asked for a spell with no targets.
            StackEntry retargeted = subjectResolutionService.spellOnStack(gameData, entry, retarget.spell());
            if (retarget.onlyIfItHasTargets() && (retargeted == null || (retargeted.getTargetId() == null
                    && retargeted.getDeclaredTargetIds().isEmpty() && retargeted.getTargetCardIds().isEmpty()))) {
                return;
            }
            if (retargeted != null) {
                targetId = retargeted.getTargetableId();
            }
        }

        // CR 603.5 — "you may" choice happens at resolution time.
        // Set flag so the resolution loop re-runs this effect after the player responds.
        gameData.resolvingMayEffectFromStack = true;
        UUID choicePlayerId = e.choicePlayerId() != null ? e.choicePlayerId() : switch (e.choicePlayer()) {
            case CONTROLLER -> entry.getControllerId();
            // Triggered abilities snapshot the active player on their stack entry. Activated
            // abilities do not need that trigger snapshot, so use the current active player for
            // effects such as Obeka's "the player whose turn it is may ...".
            case ACTIVE_PLAYER -> entry.getActivePlayerId() != null
                    ? entry.getActivePlayerId() : gameData.activePlayerId;
            case DEFENDING_PLAYER -> findDefendingPlayerId(gameData, entry);
            case TARGET_PLAYER -> targetId != null && gameData.playerIds.contains(targetId) ? targetId : null;
            case TARGET_PERMANENT_CONTROLLER -> targetPermanentController(gameData, entry, targetId);
            case TARGET_SPELL_CONTROLLER -> {
                UUID onStack = findTargetSpellControllerId(gameData, targetId);
                yield onStack != null ? onStack : entry.getCounteredSpellControllerId();
            }
            case TRIGGERING_PERMANENT_CONTROLLER -> findTriggeringPermanentControllerId(gameData, entry, targetId);
            case TARGET_PLAYER_OR_PERMANENT_CONTROLLER -> targetId == null
                    ? null
                    : gameData.playerIds.contains(targetId)
                    ? targetId
                    : gameQueryService.findPermanentController(gameData, targetId);
            case TRIGGERING_SPELL_CONTROLLER -> targetId;
            case SOURCE_OWNER -> sourceOwnerId(entry);
        };
        if (choicePlayerId == null) {
            gameData.resolvingMayEffectFromStack = false;
            return;
        }
        CardEffect optionalAction = e.wrapped() instanceof SequenceEffect sequence
                && !sequence.steps().isEmpty() ? sequence.steps().getFirst() : e.wrapped();
        if (optionalAction instanceof TargetPlayerExilesCardFromGraveyardEffect graveyardExile
                && graveyardExile.filter() == null && choicePlayerId.equals(targetId)
                && gameData.playerGraveyards.getOrDefault(targetId, List.of()).isEmpty()) {
            // "Target opponent may exile a card from their graveyard. If that player doesn't, ...": with no card in
            // that graveyard the player can't choose to exile one (CR 608.2d), so "if that player doesn't" applies.
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }
        if (optionalAction instanceof GainControlOfTargetEffect gain && gain.gainer() != null
                && gain.gainer().declaresTarget() && gain.subject() instanceof PermanentSubject.Target
                && (subjectResolutionService.players(gameData, entry, gain, gain.gainer()).isEmpty()
                        || entry.targetsForEffect(gain).stream()
                                .noneMatch(id -> gameQueryService.findPermanentById(gameData, id) != null))) {
            // "You may have target opponent gain control of target permanent you control": with either target illegal
            // the control change can't happen (CR 608.2b), so there is no choice to offer (Iroh, Tea Master's ruling).
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }
        UUID discarderId = optionalAction instanceof DiscardEffect discard
                ? discard.player() instanceof PlayerSubject.You ? entry.getControllerId()
                : discard.player().declaresTarget() && !discard.random() && !entry.isNonTargeting() ? targetId : null
                : null;
        if (discarderId == null && optionalAction instanceof DiscardEffect namedDiscard
                && !namedDiscard.player().declaresTarget() && !(namedDiscard.player() instanceof PlayerSubject.Each)) {
            // "That player may discard a card": judged only when the deciding player is the one who discards.
            UUID namedDiscarderId = subjectResolutionService.player(gameData, entry, namedDiscard, namedDiscard.player());
            discarderId = choicePlayerId.equals(namedDiscarderId) ? namedDiscarderId : null;
        }
        if (discarderId != null && optionalAction instanceof DiscardEffect discard
                && countDiscardable(gameData, entry, discarderId, discard.filter()) < requiredDiscards(gameData, entry, discard)) {
            // "You may discard [N] [land] card(s)": a player with fewer such cards can't choose that option (CR 608.2d;
            // the discard is a cost of "if you do", CR 118.12, paid only in full, CR 118.3), so no discard choice is
            // offered and "if you don't" applies. A random discard, or a discard by "that player" from a nontargeting
            // combat damage trigger, is still asked (CR 603.5).
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }
        if (optionalAction instanceof DiscardCardThenEffect discardThen && !discardThen.thenWithoutDiscard()
                && discardThen.player() instanceof PlayerSubject.You
                && countDiscardable(gameData, entry, entry.getControllerId(), discardThen.filter()) == 0) {
            // "You may discard a [kind] card. When you do, ..." and "Sacrifice this unless you discard a card": with
            // nothing that could be discarded, the choice isn't offered and any penalty happens (CR 118.12a, 608.2d).
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }
        if (optionalAction instanceof DrawCardEffect optionalDraw && optionalDraw.player() instanceof PlayerSubject.You
                && !drawThenDiscard
                && drawService.isDrawPrevented(gameData, entry.getControllerId())) {
            // The ability's controller is the one who would draw, whoever makes the choice.
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }
        if (optionalAction instanceof SearchLibraryEffect search
                && choicePlayerId.equals(subjectResolutionService.player(gameData, entry, search, search.searchPlayer()))
                && LibrarySearchRestrictions.find(gameData, gameQueryService, choicePlayerId, choicePlayerId,
                        entry.getControllerId()) != null) {
            // "You may search your library ..., then shuffle": a player who can't search can't choose to, so nothing
            // is searched or shuffled (Leonin Arbiter's ruling, CR 608.2d).
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }
        if (optionalAction instanceof RemoveCountersEffect removal
                && removal.subject() instanceof PermanentSubject.Source
                && !removal.all() && removal.dynamicAmount() == null && !removal.targetsPlayer() && removal.amount() > 0
                && !sourceHasCountersToRemove(gameData, entry, removal)) {
            // "You may remove a [kind] counter from this creature. If you don't, ...": with fewer such counters on it,
            // or with it gone, the player can't choose to remove them (CR 608.2d), so "if you don't" applies.
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }
        boolean cannotSacrifice = e.wrapped() instanceof SacrificeSelfEffect sacrificeAttached
                && (sacrificeAttached.subject() instanceof PermanentSubject.Attached
                || sacrificeAttached.subject() instanceof PermanentSubject.AttachedAtTrigger)
                && !canSacrificeEnchantedPermanent(gameData, entry, sacrificeAttached, choicePlayerId);
        if (optionalAction instanceof SacrificeSelfEffect sacrificeSelf && sacrificeSelfThenIfSacrificed(e.wrapped())) {
            // "You may sacrifice it. If you do, ...": a permanent the player can't sacrifice isn't offered.
            UUID sacrificedId = sacrificeSelf.subject() instanceof PermanentSubject.TriggeringOrSource
                    && entry.getTriggeringPermanentId() != null
                    ? entry.getTriggeringPermanentId() : entry.getSourcePermanentId();
            var source = sacrificedId == null ? null : gameQueryService.findPermanentById(gameData, sacrificedId);
            cannotSacrifice = source == null
                    || !choicePlayerId.equals(gameQueryService.findPermanentController(gameData, source.getId()))
                    || gameQueryService.cantBeSacrificed(gameData, source);
        }
        if (optionalAction instanceof SacrificePermanentsEffect sacrifice
                && sacrifice.count() instanceof Fixed count
                && count.value() > 0
                && (sacrifice.player().declaresTarget() || sacrifice.player() instanceof PlayerSubject.You)) {
            UUID sacrificingPlayerId = sacrifice.player().declaresTarget() && targetId != null
                    ? targetId : choicePlayerId;
            // "You may sacrifice N [permanents]" (alone or leading "If you do, ..."): a player who can't sacrifice
            // all N can't choose that option (CR 608.2d). The choice is only withheld when the deciding player is
            // the one who would sacrifice; a controller choosing for a targeted opponent is asked whatever that
            // opponent controls.
            cannotSacrifice = choicePlayerId.equals(sacrificingPlayerId)
                    && !sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(
                    gameData, entry, sacrifice, sacrificingPlayerId);
        }
        if (optionalAction instanceof SacrificePermanentsEffect sacrifice
                && sacrifice.player() instanceof PlayerSubject.ControllerOf) {
            // "That creature's controller may sacrifice [N] [permanents]" (that many, Archfiend of Spite): judged the
            // same way when the deciding player is the one who sacrifices (CR 608.2d; the rulings: a player who
            // can't sacrifice that many must take the other branch).
            UUID sacrificingPlayerId = subjectResolutionService.player(gameData, entry, sacrifice, sacrifice.player());
            cannotSacrifice = choicePlayerId.equals(sacrificingPlayerId)
                    && !sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(
                    gameData, entry, sacrifice, sacrificingPlayerId);
        }
        if (cannotSacrifice) {
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }

        boolean defendingPlayerChoice = e.choicePlayer() == MayChoicePlayer.DEFENDING_PLAYER;
        if (e.choicePlayer() == MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER) {
            // The resolved effect acts for the triggering permanent's controller as of resolution, which
            // can differ from the trigger-time controller if control changed while the ability waited.
            entry.setTriggeringPermanentControllerId(choicePlayerId);
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                defendingPlayerChoice ? entry.getControllerId() : choicePlayerId,
                List.of(e.wrapped()),
                entry.getCard().getName() + " - " + e.prompt(),
                targetId,
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                attackedTargetForPendingMay(gameData, entry),
                e.choicePlayer() == MayChoicePlayer.ACTIVE_PLAYER ? entry.getActivePlayerId() : null,
                defendingPlayerChoice ? choicePlayerId : null,
                entry.getSourcePermanentSnapshot(),
                e.choicePlayer() == MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER
                        ? choicePlayerId : entry.getTriggeringPermanentControllerId(),
                entry.getTriggeringCardId(),
                entry.getEventValue(),
                entry.getTriggeringPermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                null,
                entry.getTriggeringPermanentToughnessAtTrigger(),
                entry.getTriggeringCardGraveyardEntryVersion()
        ).withGrantingPermanentId(entry.getGrantingPermanentId())
                .withTriggeringSpell(entry.getTriggeringSpellSnapshot())
                .withSourceGraveyardEntryVersion(entry.getSourceGraveyardEntryVersion()));
    }

    /** Whether counters of {@code type} could be put on {@code permanent} (CR 608.2d: else there is no choice). */
    private boolean canHaveCounters(GameData gameData, Permanent permanent, CounterType type) {
        if (gameQueryService.cantHaveCounters(gameData, permanent)) {
            return false;
        }
        return switch (type) {
            case PLUS_ONE_PLUS_ONE -> !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent);
            case MINUS_ONE_MINUS_ONE -> !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent);
            default -> true;
        };
    }

    /** Whether the resolving entry's source permanent has the counters {@code removal} would remove. */
    private boolean sourceHasCountersToRemove(GameData gameData, StackEntry entry, RemoveCountersEffect removal) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return false;
        }
        if (removal.counterType() == CounterType.ANY) {
            return source.getCounters().values().stream().anyMatch(count -> count >= removal.amount());
        }
        return source.getCounterCount(removal.counterType()) >= removal.amount();
    }

    /** How many cards in {@code playerId}'s hand a discard may choose: those matching {@code filter}, or all of them. */
    private int countDiscardable(GameData gameData, StackEntry entry, UUID playerId,
                                 com.github.laxika.magicalvibes.model.filter.CardPredicate filter) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        if (filter == null) {
            return hand.size();
        }
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        return (int) hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, sourceCardId, gameData, entry.getControllerId()))
                .count();
    }

    /** How many cards an optional discard asks for: its count as the choice is offered, at least one. */
    private int requiredDiscards(GameData gameData, StackEntry entry, DiscardEffect discard) {
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int amount = amountEvaluationService.evaluate(gameData, discard.amount(),
                AmountContext.forStackEntry(entry, source != null ? source : entry.getSourcePermanentSnapshot()));
        return Math.max(1, amount);
    }

    private boolean hasOwnGraveyardCardsToExile(GameData gameData, StackEntry entry, ExileGraveyardCardsEffect exile) {
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        return gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(card -> exile.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        card, exile.filter(), sourceCardId, gameData, entry.getControllerId()))
                .count() >= Math.max(1, exile.count());
    }

    /**
     * CR 508.5: the defending player stays the player the creature attacked even after an attacked
     * planeswalker or battle has left the battlefield, so fall back to the player recorded at trigger time.
     */
    private UUID attackedTargetForPendingMay(GameData gameData, StackEntry entry) {
        UUID attacked = entry.getAttackedTargetId();
        if (attacked != null && entry.getDefendingPlayerId() != null
                && !gameData.playerIds.contains(attacked)
                && gameQueryService.findPermanentById(gameData, attacked) == null) {
            return entry.getDefendingPlayerId();
        }
        return attacked;
    }

    /**
     * Whether the leading step reads the resolution's picks ({@link PermanentSubject.Picked}: what it transforms, the
     * attachment it attaches, or the attachment a choice of its host must fit) and they name no permanent.
     */
    private boolean picksNameNothing(GameData gameData, StackEntry entry, CardEffect leading) {
        PermanentSubject picks = leading instanceof TransformSelfEffect transform ? transform.subject()
                : leading instanceof AttachSourceEquipmentToTargetCreatureEffect attach ? attach.equipment()
                : leading instanceof ChoosePermanentsEffect choose ? choose.hostFor()
                : null;
        return picks instanceof PermanentSubject.Picked
                && subjectResolutionService.permanents(gameData, entry, leading, picks).isEmpty();
    }

    private void insertElseEffect(StackEntry entry, MayEffect may) {
        if (may.elseEffect() != null) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(may.elseEffect()));
        }
    }

    private boolean controlsBlightableCreature(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .anyMatch(permanent -> !gameQueryService.cantHaveCounters(gameData, permanent)
                        && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)
                        && !gameQueryService.cantBeChosen(gameData, permanent));
    }

    /** Whether "that player" can sacrifice the permanent the source Aura enchants as the choice is offered. */
    private boolean canSacrificeEnchantedPermanent(GameData gameData, StackEntry entry, SacrificeSelfEffect sacrifice,
                                                   UUID choicePlayerId) {
        var enchanted = subjectResolutionService.permanent(gameData, entry, sacrifice, sacrifice.subject());
        return enchanted != null
                && choicePlayerId.equals(gameQueryService.findPermanentController(gameData, enchanted.getId()))
                && !gameQueryService.cantBeSacrificed(gameData, enchanted);
    }

    private UUID targetPermanentController(GameData gameData, StackEntry entry, UUID targetId) {
        if (targetId == null) return null;
        UUID current = gameQueryService.findPermanentController(gameData, targetId);
        return current == null ? entry.getRemovedPermanentControllers().get(targetId) : current;
    }

    private UUID findTriggeringPermanentControllerId(GameData gameData, StackEntry entry, UUID fallback) {
        if (entry.getTriggeringPermanentId() != null) {
            UUID liveControllerId = gameQueryService.findPermanentController(gameData, entry.getTriggeringPermanentId());
            if (liveControllerId != null) {
                return liveControllerId;
            }
            // Its last known controller once it has left (CR 608.2h).
            UUID departedControllerId = entry.getRemovedPermanentControllers().get(entry.getTriggeringPermanentId());
            if (departedControllerId != null) {
                return departedControllerId;
            }
        }
        return entry.getTriggeringPermanentControllerId() != null
                ? entry.getTriggeringPermanentControllerId() : fallback;
    }

    private UUID sourceOwnerId(StackEntry entry) {
        return entry.getCard() != null && entry.getCard().getOwnerId() != null
                ? entry.getCard().getOwnerId() : entry.getControllerId();
    }

    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getTargetableId().equals(targetCardId)) {
                return stackEntry.getControllerId();
            }
        }
        return null;
    }

    private boolean hasLegalAuraAttachment(GameData gameData, Card aura, UUID controllerId) {
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (var permanent : battlefield) {
                if (auraAttachmentService.canEnchant(gameData, aura, controllerId, permanent)) return true;
            }
        }
        return aura.isEnchantPlayer() && gameData.orderedPlayerIds.stream()
                .anyMatch(playerId -> auraAttachmentService.canEnchantPlayer(gameData, aura, controllerId, playerId));
    }

    /**
     * The defending player of an attack trigger: the one recorded as it triggered, who stays the defending player
     * after an attacked planeswalker or battle left the battlefield (CR 508.5), else the attacked player, the attacked
     * planeswalker's controller or the attacked battle's protector, as {@code Subjects.defendingPlayer()} reads it.
     */
    private UUID findDefendingPlayerId(GameData gameData, StackEntry entry) {
        return entry.getDefendingPlayerId() != null
                ? entry.getDefendingPlayerId()
                : gameQueryService.defendingPlayerOfAttackTarget(gameData, entry.getAttackedTargetId());
    }

    /**
     * Whether {@code wrapped} is "sacrifice this (or the triggering) permanent. If you do, ...": a sequence of a
     * {@link SacrificeSelfEffect} of the source, or of the triggering permanent else the source, then a step gated on
     * the sacrifice having happened.
     */
    private static boolean sacrificeSelfThenIfSacrificed(CardEffect wrapped) {
        return wrapped instanceof SequenceEffect sequence && sequence.steps().size() == 2
                && sequence.steps().getFirst() instanceof SacrificeSelfEffect sacrifice
                && (sacrifice.subject() instanceof PermanentSubject.Source
                || sacrifice.subject() instanceof PermanentSubject.TriggeringOrSource)
                && sequence.steps().get(1) instanceof ConditionalEffect gate
                && gate.condition() instanceof HappenedThisWay happened && happened.kind() == StepKind.SACRIFICE;
    }
}
