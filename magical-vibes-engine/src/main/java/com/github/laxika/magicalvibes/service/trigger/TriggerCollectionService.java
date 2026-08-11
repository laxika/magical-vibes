package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.service.input.PlayerInputService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DelayedEffectOnDeath;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealsDamage;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeSourceWhenTargetLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.LifeGainOpponentLifeLossWatcher;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.CreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageDamagedCreatureControllerAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EquipmentDamagesOtherDefendingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EquipmentTapsAndLocksDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectAllyDamageToDamagedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndSkipUntapDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellReferencingEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.CombatDamageTriggerContextEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCardAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCounterAwareEffect;
import com.github.laxika.magicalvibes.model.effect.StormCopyEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.effect.ClashOutcomeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellingEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterCreatureConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardNameMatchesEnteringPermanent;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentFirstSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToManaSpentToCastToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncrementTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thin orchestrator that detects trigger events, iterates permanents/effect-slots,
 * and delegates per-effect handling to the {@link TriggerCollectorRegistry}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerCollectionService {

    private final TriggerCollectorRegistry registry;
    private final GameOutcomeService gameOutcomeService;
    private final PlayerInputService playerInputService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;

    public List<CardEffect> grantedTriggeredEffects(GameData gameData, Permanent permanent, EffectSlot slot) {
        return grantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, permanent, slot);
    }

    // ── Spell-cast triggers ────────────────────────────────────────────

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, true);
    }

    /**
     * Fires delayed "until end of turn, whenever you cast a [filter] spell, …" triggers registered
     * this turn (Mountain Titan). One trigger per registration; a registration whose source permanent
     * has left the battlefield is skipped.
     */
    private void processDelayedControllerSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        if (!gameData.hasDelayedAction(DelayedControllerSpellCastTrigger.class)) {
            return;
        }
        for (DelayedControllerSpellCastTrigger delayed
                : gameData.getDelayedActions(DelayedControllerSpellCastTrigger.class)) {
            if (!delayed.controllerId().equals(castingPlayerId)) continue;
            Permanent source = gameQueryService.findPermanentById(gameData, delayed.sourcePermanentId());
            if (source == null) continue;
            if (!predicateEvaluationService.matchesCardPredicate(spellCard, delayed.spellFilter(), null)) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(delayed.resolvedEffects()),
                    null,
                    delayed.sourcePermanentId());
            entry.setNonTargeting(true);
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.cardTextCard(
                    delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
            log.info("Game {} - {} delayed spell-cast trigger fires for {}",
                    gameData.id, delayed.sourceCard().getName(), spellCard.getName());
        }
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFromHand) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId,
                castFromHand ? Zone.HAND : Zone.GRAVEYARD);
    }

    /**
     * Zone-carrying form: {@code castZone} is the zone the spell was cast from, so triggers that
     * care about a specific origin (cast from a graveyard, cast from the top of a library) can tell
     * them apart instead of sharing one "not from hand" flag.
     */
    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone) {
        var ctx = new TriggerContext.SpellCast(spellCard, castingPlayerId, castZone);

        // Opening hand reveal delayed triggers (Chancellor cycle)
        if (!gameData.openingHandRevealTriggers.isEmpty()
                && !gameData.playersWhoCastFirstSpellInGame.contains(castingPlayerId)) {
            gameData.playersWhoCastFirstSpellInGame.add(castingPlayerId);
            for (OpeningHandRevealTrigger trigger : gameData.openingHandRevealTriggers) {
                if (!trigger.revealingPlayerId().equals(castingPlayerId)
                        && trigger.effect() instanceof CounterUnlessEffect counterEffect) {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            trigger.sourceCard(),
                            trigger.revealingPlayerId(),
                            trigger.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(counterEffect)),
                            spellCard.getId(),
                            Zone.STACK
                    );
                    gameData.stack.add(entry);
                }
            }
        }

        // ON_ANY_PLAYER_CASTS_SPELL
        gameData.forEachPermanent((playerId, perm) -> {
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, ctx);
        });

        // ON_CONTROLLER_CASTS_SPELL (only controller's own spells)
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_CONTROLLER_CASTS_SPELL, ctx);
        });

        processDelayedControllerSpellCastTriggers(gameData, spellCard, castingPlayerId);

        // ON_OPPONENT_CASTS_SPELL (only opponents' permanents)
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(castingPlayerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_CASTS_SPELL, ctx);
        });

        // Increment keyword (CR keyword): "Whenever you cast a spell, if the amount of mana you spent
        // is greater than this creature's power or toughness, put a +1/+1 counter on it." Driven by the
        // Scryfall-loaded keyword (like Undying) rather than a per-card effect. Read before mana-spent
        // is cleared below.
        collectIncrementTriggers(gameData, spellCard, castingPlayerId);

        // Emblem spell cast triggers (e.g. Venser's emblem, Jace Unraveler of Secrets' emblem,
        // Chandra Dressed to Kill's emblem). Mana-spent readers must run before clearSpellCastManaSpent.
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof ExileTargetOnControllerSpellCastEffect) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    gameData.queueInteraction(new PermanentChoiceContext.EmblemTriggerTarget(
                            "Venser's emblem",
                            emblem.controllerId(),
                            List.of(new ExileTargetPermanentEffect()),
                            emblem.sourceCard()
                    ));
                } else if (effect instanceof CounterOpponentFirstSpellEachTurnEffect) {
                    // Opponent's first spell this turn — auto-target it on the stack (no choice).
                    if (emblem.controllerId().equals(castingPlayerId)) continue;
                    if (gameData.getSpellsCastThisTurnCount(castingPlayerId) != 1) continue;
                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Jace, Unraveler of Secrets")
                            + "'s emblem";
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source != null ? source : spellCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(new CounterSpellEffect())),
                            spellCard.getId(),
                            Zone.STACK
                    ));
                    gameLogService.append(gameData,
                            GameLog.text(desc + " triggers — counter that spell."));
                    log.info("Game {} - {} counters opponent's first spell this turn",
                            gameData.id, desc);
                } else if (effect instanceof SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect) {
                    // Garruk, Caller of Beasts' emblem — fires on the controller's own creature spells.
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (!spellCard.hasType(CardType.CREATURE)) continue;
                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Garruk, Caller of Beasts")
                            + "'s emblem";
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source != null ? source : spellCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(new MayEffect(
                                    new SearchLibraryEffect(
                                            new CardTypePredicate(CardType.CREATURE),
                                            LibrarySearchDestination.BATTLEFIELD),
                                    "Search your library for a creature card and put it onto the battlefield?")))
                    ));
                    gameLogService.append(gameData, GameLog.text(desc + " triggers."));
                    log.info("Game {} - {} creature-spell search trigger queued", gameData.id, desc);
                } else if (effect instanceof MillEffect mill && mill.recipient() == MillRecipient.TARGET_PLAYER) {
                    // "Whenever you cast a spell, target opponent mills N cards" (Jace, Telepath
                    // Unbound's emblem) — the opponent restriction rides the player target filter.
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard,
                            emblem.controllerId(),
                            new ArrayList<>(List.of(mill)),
                            true,
                            new PlayerPredicateTargetFilter(
                                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                    "Target must be an opponent")
                    ));
                    gameLogService.append(gameData, GameLog.text(
                            (source != null ? source.getName() : "Emblem")
                                    + "'s emblem triggers — choose target opponent."));
                    log.info("Game {} - emblem mill trigger queued", gameData.id);
                } else if (effect instanceof DealDamageEqualToManaSpentToCastToAnyTargetEffect damageTrigger) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (damageTrigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(
                                    spellCard, damageTrigger.spellFilter(), null)) {
                        continue;
                    }
                    int manaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    String desc = (source != null ? source.getName() : "Chandra, Dressed to Kill")
                            + "'s emblem";
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard,
                            emblem.controllerId(),
                            new ArrayList<>(List.of(new DealDamageToAnyTargetEffect(manaSpent)))
                    ));
                    gameLogService.append(gameData,
                            GameLog.text(desc + " triggers — choose a target for " + manaSpent + " damage."));
                    log.info("Game {} - {} emblem mana-spent damage trigger queued ({} damage)",
                            gameData.id, desc, manaSpent);
                }
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        gameData.clearSpellCastManaSpent(spellCard.getId());

        // GRAVEYARD_ON_CONTROLLER_CASTS_SPELL — graveyard-resident spell-cast triggers
        // (e.g. Lingering Phantom: "Whenever you cast a historic spell, you may pay {B}. If you do, return ~ to hand.")
        List<Card> castingPlayerGraveyard = gameData.playerGraveyards.get(castingPlayerId);
        if (castingPlayerGraveyard != null) {
            for (Card card : new ArrayList<>(castingPlayerGraveyard)) {
                List<CardEffect> graveyardEffects = card.getEffects(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL);
                if (graveyardEffects == null || graveyardEffects.isEmpty()) continue;

                for (CardEffect effect : graveyardEffects) {
                    if (effect instanceof SpellCastTriggerEffect trigger) {
                        if (!predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) continue;

                        if (trigger.manaCost() != null) {
                            // "you may pay {X}" pattern — queue MayPayManaEffect on the stack
                            CardEffect resolvedEffect = trigger.resolvedEffects().getFirst();
                            MayPayManaEffect mayPay = new MayPayManaEffect(
                                    trigger.manaCost(),
                                    resolvedEffect,
                                    "Pay " + trigger.manaCost() + " to return " + card.getName()
                                            + " from your graveyard to your hand?"
                            );
                            gameData.queueMayAbility(card, castingPlayerId, mayPay, null);
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    card,
                                    castingPlayerId,
                                    card.getName() + "'s ability",
                                    new ArrayList<>(trigger.resolvedEffects())
                            ));
                        }

                        log.info("Game {} - {} graveyard spell-cast trigger queued",
                                gameData.id, card.getName());
                    }
                }
            }
        }

        // COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL — Eminence and similar command-zone spell-cast triggers
        List<Card> castingPlayerCommandZone = gameData.playerCommandZones.get(castingPlayerId);
        if (castingPlayerCommandZone != null) {
            for (Card card : new ArrayList<>(castingPlayerCommandZone)) {
                List<CardEffect> commandEffects = card.getEffects(EffectSlot.COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL);
                if (commandEffects == null || commandEffects.isEmpty()) continue;

                for (CardEffect effect : commandEffects) {
                    if (effect instanceof SpellCastTriggerEffect trigger) {
                        if (!predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                castingPlayerId,
                                card.getName() + "'s ability",
                                new ArrayList<>(trigger.resolvedEffects())
                        ));
                        log.info("Game {} - {} command-zone spell-cast trigger queued",
                                gameData.id, card.getName());
                    }
                }
            }
        }

        // Primal Wellspring delayed mana trigger: copy next instant/sorcery (one-shot)
        Integer pendingCopies = gameData.pendingNextInstantSorceryCopyCount.get(castingPlayerId);
        if (pendingCopies != null && pendingCopies > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int copyNumber = 0; copyNumber < pendingCopies; copyNumber++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName() + " (Primal Wellspring)",
                            new ArrayList<>(List.of(copyEffect))
                    ));
                }
                gameData.pendingNextInstantSorceryCopyCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Primal Wellspring)."));
                log.info("Game {} - {} spell-copy trigger(s) queued for {} (Primal Wellspring)",
                        gameData.id, pendingCopies, spellCard.getName());
            }
        }

        // Pyromancer's Goggles delayed mana trigger: copy next *red* instant/sorcery (one-shot)
        Integer pendingRedCopies = gameData.pendingNextRedInstantSorceryCopyCount.get(castingPlayerId);
        if (pendingRedCopies != null && pendingRedCopies > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))
                && spellCard.getColors() != null && spellCard.getColors().contains(CardColor.RED)) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int copyNumber = 0; copyNumber < pendingRedCopies; copyNumber++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(copyEffect))
                    ));
                }
                gameData.pendingNextRedInstantSorceryCopyCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} red spell-copy trigger(s) queued for {}",
                        gameData.id, pendingRedCopies, spellCard.getName());
            }
        }

        // "When you next cast an instant or sorcery spell this turn, copy that spell"
        // (e.g. Chandra, the Firebrand −2). Same one-shot shape as Primal Wellspring's trigger, but
        // tracked in the turn-scoped counter so it survives mana drain.
        Integer pendingTurnCopies = gameData.pendingNextInstantSorceryCopyThisTurnCount.get(castingPlayerId);
        if (pendingTurnCopies != null && pendingTurnCopies > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                List<CardEffect> copyEffects = new ArrayList<>(pendingTurnCopies);
                for (int i = 0; i < pendingTurnCopies; i++) {
                    copyEffects.add(new CopyControllerCastSpellEffect(snapshot, castingPlayerId));
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName(),
                        copyEffects
                ));
                gameData.pendingNextInstantSorceryCopyThisTurnCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} delayed spell-copy trigger(s) queued for {}",
                        gameData.id, pendingTurnCopies, spellCard.getName());
            }
        }

        // "Whenever you cast a creature spell this turn, draw a card" (Glimpse of Nature).
        // Repeating for the rest of the turn; multiple copies draw one card each.
        Integer creatureCastDraws = gameData.creatureSpellCastDrawsThisTurn.get(castingPlayerId);
        if (creatureCastDraws != null && creatureCastDraws > 0 && spellCard.hasType(CardType.CREATURE)) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    spellCard,
                    castingPlayerId,
                    "Draw a card",
                    new ArrayList<>(List.of(new DrawCardEffect(creatureCastDraws)))
            ));
            log.info("Game {} - creature-spell-cast draw trigger queued for {} ({} cards)",
                    gameData.id, castingPlayerId, creatureCastDraws);
        }

        // "Until end of turn, whenever you cast an instant or sorcery spell, copy it"
        // (e.g. The Mirari Conjecture chapter III)
        if (gameData.playersWithSpellCopyUntilEndOfTurn.contains(castingPlayerId)
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            // Find the spell on the stack to create a snapshot
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                CopyControllerCastSpellEffect copyEffect =
                        new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName(),
                        new ArrayList<>(List.of(copyEffect))
                ));
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (The Mirari Conjecture)."));
                log.info("Game {} - {} spell-copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            }
        }

        // Conspire (CR 702.78): "When you [tap the two creatures], copy it and you may choose a new
        // target for the copy." The spell was flagged in gameData.conspiredSpellIds when its conspire
        // cost was paid during casting. One copy per conspired spell.
        if (gameData.conspiredSpellIds.remove(spellCard.getId())) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                CopyControllerCastSpellEffect copyEffect =
                        new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName() + " (Conspire)",
                        new ArrayList<>(List.of(copyEffect))
                ));
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Conspire)."));
                log.info("Game {} - {} conspire copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            }
        }

        // ON_SELF_CAST — "When you cast this spell, ..." triggers scanned against the just-cast card
        // itself (it's a spell on the stack, not a permanent). CopyThisSpellIfConditionEffect (SOS
        // Infusion copy cycle) needs a snapshot of the spell entry; any other effect (e.g. Demigod of
        // Revenge's graveyard return) is queued as a plain triggered ability under the caster.
        List<CardEffect> selfCastTriggeredEffects = new ArrayList<>();
        for (CardEffect effect : spellCard.getEffects(EffectSlot.ON_SELF_CAST)) {
            if (effect instanceof CopyThisSpellIfConditionEffect trigger) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getCard().getId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                // Always triggers; the "if <condition>" is an effect clause re-checked at resolution.
                StackEntry snapshot = new StackEntry(spellEntry);
                CardEffect copyEffect = new ConditionalEffect(trigger.condition(),
                        new CopyControllerCastSpellEffect(snapshot, castingPlayerId));
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(copyEffect))
                ));
                log.info("Game {} - {} self-cast copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            } else if (effect instanceof StormEffect) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getCard().getId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                // "for each spell cast before it this turn" — this spell is already recorded, so
                // subtract it out. Count is fixed here (spells cast after can't precede this one).
                int copies = gameData.getTotalSpellsCastThisTurnCount() - 1;
                StackEntry snapshot = new StackEntry(spellEntry);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new StormCopyEffect(snapshot, castingPlayerId, copies)))
                ));
                log.info("Game {} - {} Storm trigger queued ({} copies) for {}",
                        gameData.id, spellCard.getName(), copies, castingPlayerId);
            } else {
                selfCastTriggeredEffects.add(effect);
            }
        }
        if (!selfCastTriggeredEffects.isEmpty()) {
            // Targeted ON_SELF_CAST (e.g. Abundant Maw: "target opponent loses 3 life") chooses
            // targets as the ability goes on the stack — reuse SpellTargetTriggerAnyTarget.
            // Multi-target ("up to N target permanents", Elder Deep-Fiend) reuses the ETB
            // multi-target slot walker with a null source permanent id.
            boolean needsPlayerTarget = selfCastTriggeredEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            boolean needsPermanentTarget = selfCastTriggeredEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (needsPlayerTarget || needsPermanentTarget) {
                boolean multiTarget = spellCard.getSpellTargets().size() > 1
                        || etbTokenTargetService.needsSlotBySlotTargetSelection(spellCard);
                if (multiTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            spellCard, castingPlayerId, new ArrayList<>(selfCastTriggeredEffects),
                            null, new ArrayList<>(), 0, 0));
                    log.info("Game {} - {} self-cast multi-target trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                } else {
                    boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            spellCard, castingPlayerId, new ArrayList<>(selfCastTriggeredEffects),
                            playerTargetOnly, spellCard.getTargetFilter()));
                    log.info("Game {} - {} self-cast targeting trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            } else {
                // Carry the spell's X onto the trigger so "reveal the top X cards" (Genesis Hydra)
                // sees the value locked in on cast (CR 601.2b); 0 for spells without {X}.
                int selfCastX = 0;
                for (StackEntry se : gameData.stack) {
                    if (se.getCard().getId().equals(spellCard.getId())) {
                        selfCastX = se.getXValue();
                        break;
                    }
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(selfCastTriggeredEffects),
                        selfCastX
                ));
                log.info("Game {} - {} self-cast trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            }
        }

        // "The first spell you cast each turn has cascade" (Maelstrom Nexus). A permanent-granted
        // keyword, detected by the presence of a GRANT_CASCADE_TO_FIRST_SPELL slot on the caster's
        // battlefield rather than an effect-type check. recordSpellCast runs before this method in
        // every cast path, so a count of 1 identifies the caster's first spell of the turn. The held
        // CascadeEffect is queued keyed to the just-cast spell so CascadeEffectHandler's threshold is
        // the spell's mana value (not the granting permanent's). One trigger per granting permanent.
        if (gameData.getSpellsCastThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    List<CardEffect> grantEffects = perm.getCard().getEffects(EffectSlot.GRANT_CASCADE_TO_FIRST_SPELL);
                    if (grantEffects.isEmpty()) continue;

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(grantEffects)
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to first spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /** Fires effects that care when the given player controls an effect that counters a spell. */
    public void checkControllerCountersSpellTriggers(GameData gameData, UUID counteringPlayerId) {
        if (counteringPlayerId == null) return;

        var ctx = new TriggerContext.SpellCountered(counteringPlayerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(counteringPlayerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_CONTROLLER_COUNTERS_SPELL, ctx);
        });
    }

    // ── Discard triggers ───────────────────────────────────────────────

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard) {
        // Central discard hook: every discard path routes through here, so count discards per player
        // for this turn (Dream Salvage's "cards target opponent discarded this turn").
        gameData.cardsDiscardedThisTurn.merge(discardingPlayerId, 1, Integer::sum);
        // Also remember which specific cards were discarded (cycling is a discard) so a later effect can
        // return them from the graveyard (Shadow of the Grave).
        // Remember the mana value of the card just discarded, so a later effect of the same spell can
        // scale off it (Blast of Genius's "damage equal to the discarded card's mana value").
        if (discardedCard != null) {
            gameData.lastDiscardedCardManaValue = discardedCard.getManaValue();
        }
        if (discardedCard != null && !discardedCard.isToken()) {
            gameData.cardsDiscardedOrCycledThisTurn
                    .computeIfAbsent(discardingPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(discardedCard.getId());
        }

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.Discard(discardingPlayerId, discardedCard);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(discardingPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DISCARDS)) {
                    // "Whenever an opponent discards a creature/land/noncreature-nonland card, …"
                    // (Waste Not) — the discarded card gates the trigger.
                    CardEffect resolved = discardedCard == null
                            ? (effect instanceof TriggeringCardConditionalEffect ? null : effect)
                            : unwrapTriggeringCardConditional(effect, discardedCard, gameData, playerId);
                    if (resolved == null) continue;
                    var match = new TriggerMatchContext(gameData, perm, playerId, resolved);
                    if (dispatch(match, EffectSlot.ON_OPPONENT_DISCARDS, resolved, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        // "Whenever you discard a card" — scan the discarding player's own battlefield (e.g. Necropotence).
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DISCARDS)) {
                    var match = new TriggerMatchContext(gameData, perm, discardingPlayerId, effect);
                    if (dispatch(match, EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        }

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }

        // Process any pending may abilities added by discard triggers
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }

        // Check the discarded card itself for self-discard triggers
        if (discardedCard != null) {
            // "When you discard this card" — any discard (Edgar's Awakening). Non-targeting effects
            // (e.g. MayPayManaEffect) go straight onto the stack; any-target effects use the
            // DiscardTriggerAnyTarget pipeline (same as Guerrilla Tactics).
            List<CardEffect> anyDiscardTriggers = discardedCard.getEffects(EffectSlot.ON_SELF_DISCARDED);
            if (!anyDiscardTriggers.isEmpty()) {
                boolean needsAnyTarget = anyDiscardTriggers.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                if (needsAnyTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                            discardedCard, discardingPlayerId, new ArrayList<>(anyDiscardTriggers)
                    ));
                } else {
                    gameData.enqueueTrigger(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            discardedCard,
                            discardingPlayerId,
                            discardedCard.getName() + "'s ability",
                            new ArrayList<>(anyDiscardTriggers)
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(discardedCard,
                        " was discarded — its ability triggers!"));
                log.info("Game {} - {} ON_SELF_DISCARDED trigger queued", gameData.id, discardedCard.getName());
                anyTriggered[0] = true;
            }

            // Skip EnterBattlefieldOnDiscardEffect — it's a replacement effect handled earlier in the discard flow
            if (gameData.discardCausedByOpponent) {
                List<CardEffect> selfTriggers = discardedCard.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                        .filter(e -> !(e instanceof EnterBattlefieldOnDiscardEffect))
                        .toList();
                if (!selfTriggers.isEmpty()) {
                    boolean needsAnyTarget = selfTriggers.stream()
                            .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                    || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                    if (needsAnyTarget) {
                        gameData.queueInteraction(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                                discardedCard, discardingPlayerId, new ArrayList<>(selfTriggers)
                        ));
                    } else {
                        gameData.enqueueTrigger(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                discardedCard,
                                discardingPlayerId,
                                discardedCard.getName() + "'s ability",
                                new ArrayList<>(selfTriggers)
                        ));
                    }
                    gameLogService.append(gameData, GameLog.cardThen(discardedCard, " was discarded by an opponent's effect — its ability triggers!"));
                    log.info("Game {} - {} self-discard trigger queued", gameData.id, discardedCard.getName());
                }
            }
        }
    }

    // ── Source deals damage to a player (noncombat) ────────────────────

    /**
     * Queues {@link EffectSlot#ON_DAMAGE_TO_PLAYER} triggers when a permanent deals noncombat
     * damage to a player (e.g. Niv-Mizzet, Dracogenius's ping). Combat damage uses the richer
     * collector in {@code CombatDamageService} instead — do not call this from the combat path.
     */
    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId) {
        var ctx = new TriggerContext.Scry(scryingPlayerId);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(scryingPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchSlot(gameData, perm, scryingPlayerId, EffectSlot.ON_CONTROLLER_SCRIES, ctx);
        }
    }

    public void checkSourceDealsDamageToPlayerTriggers(GameData gameData, Permanent source,
                                                       UUID controllerId, UUID damagedPlayerId,
                                                       int damageDealt) {
        if (source == null || controllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
        for (CardEffect effect : effects) {
            queueNoncombatDamageToPlayerEffect(gameData, source, controllerId, damagedPlayerId, damageDealt, effect);
        }

        // Auras/Equipment attached to the source with ON_DAMAGE_TO_PLAYER (e.g. Curiosity).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!perm.isAttached() || perm.getAttachedTo() == null
                    || !perm.getAttachedTo().equals(source.getId())) {
                return;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER)) {
                // Attached triggers are controlled by the Aura/Equipment's controller.
                UUID attachedControllerId = gameData.findControllerOf(perm);
                if (attachedControllerId == null) continue;
                queueNoncombatDamageToPlayerEffect(gameData, perm, attachedControllerId, damagedPlayerId,
                        damageDealt, effect);
            }
        });
    }

    private void queueNoncombatDamageToPlayerEffect(GameData gameData, Permanent source, UUID controllerId,
                                                    UUID damagedPlayerId, int damageDealt, CardEffect effect) {
        CardEffect toQueue = effect;
        if (toQueue instanceof ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(source, controllerId))) {
                return;
            }
            toQueue = conditional.wrapped();
        }

        if (toQueue instanceof MayEffect may) {
            int mayEventValue = may.wrapped() instanceof DrawCardEffect draw
                    && draw.amount() instanceof com.github.laxika.magicalvibes.model.amount.EventValue
                    ? damageDealt : 0;
            UUID mayTargetId = may.wrapped().targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    ? null : damagedPlayerId;
            gameData.queueMayAbility(source.getCard(), controllerId, may, mayTargetId, source.getId(), mayEventValue);
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage trigger fires."));
            return;
        }

        String desc = source.getCard().getName() + "'s triggered ability";
        StackEntry se;
        CombatDamageTriggerContextEffect.TriggerContext triggerContext =
                toQueue instanceof CombatDamageTriggerContextEffect contextEffect
                        ? contextEffect.combatDamageTriggerContext()
                        : null;
        if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), damageDealt, damagedPlayerId, null);
        } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.SOURCE_SELF) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), null, source.getId());
        } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), damagedPlayerId, source.getId());
        } else {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue));
        }
        if (toQueue instanceof DiscardEffect
                || (toQueue instanceof DrawCardEffect draw
                        && draw.amount() instanceof com.github.laxika.magicalvibes.model.amount.EventValue)
                || (toQueue instanceof MillEffect mill
                        && mill.count() instanceof com.github.laxika.magicalvibes.model.amount.EventValue)) {
            se.setEventValue(damageDealt);
        }
        se.setNonTargeting(true);
        gameData.stack.add(se);
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage trigger goes on the stack."));
    }

    // ── Damage-dealt-to-controller triggers ────────────────────────────

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) {
        if (sourcePermanentId == null) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        boolean hasTrigger = false;
        for (Permanent perm : damagedPlayerBattlefield) {
            if (!perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).isEmpty()
                    || (isCombatDamage
                            && !perm.getCard().getEffects(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU).isEmpty())) {
                hasTrigger = true;
                break;
            }
        }
        if (!hasTrigger) return;

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) return;

        if (isCombatDamage) {
            queueCreatureCombatDamageToYouTriggers(gameData, damagedPlayerId, sourcePermanent);
        }

        var ctx = new TriggerContext.DamageToController(damagedPlayerId, sourcePermanentId, isCombatDamage);

        for (Permanent perm : new ArrayList<>(damagedPlayerBattlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                boolean triggered = dispatch(match, EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

                // If the source was bounced, stop processing all triggers
                if (triggered && gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                    return;
                }
            }
        }
    }

    /**
     * Puts every {@link EffectSlot#ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU} ability on the damaged
     * player's battlefield onto the stack when a creature dealt them combat damage. The whole slot
     * becomes one triggered ability per watching permanent whose {@code targetId} is the damaging
     * creature, so "destroy that creature" is expressed with {@code DestroyTargetPermanentEffect}
     * while the ability itself does not target (CR 115.10a).
     */
    private void queueCreatureCombatDamageToYouTriggers(GameData gameData, UUID damagedPlayerId,
                                                        Permanent sourceCreature) {
        if (!gameQueryService.isCreature(gameData, sourceCreature)) return;

        for (Permanent perm : new ArrayList<>(gameData.playerBattlefields.get(damagedPlayerId))) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU);
            if (effects.isEmpty()) continue;

            StackEntry se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), damagedPlayerId,
                    perm.getCard().getName() + "'s triggered ability", List.copyOf(effects),
                    sourceCreature.getId(), perm.getId());
            se.setNonTargeting(true);
            gameData.stack.add(se);
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                    "'s combat damage trigger goes on the stack."));
        }
    }

    // ── Enchanted-creature-deals-damage-to-you reflect triggers (Backfire) ──

    /**
     * Handles {@link EffectSlot#ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU} — "Whenever enchanted creature
     * deals damage to you, this Aura deals that much damage to that creature's controller" (Backfire).
     *
     * <p>The aura is on its controller's battlefield, so scanning the damaged player's battlefield for auras
     * attached to the damage source naturally restricts the trigger to damage dealt to the aura's controller
     * ("to you"). Called for both combat and non-combat damage dealt to a player. Queues a triggered ability
     * running {@link EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect}, whose handler deals
     * {@code amount} damage to the enchanted creature's controller.
     */
    public void checkEnchantedCreatureDealtDamageToControllerReflectTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (sourcePermanentId == null || amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (enchantedCreature == null) return;

        for (Permanent aura : new ArrayList<>(damagedPlayerBattlefield)) {
            if (!aura.isAttached() || !sourcePermanentId.equals(aura.getAttachedTo())) continue;
            if (aura.getCard().getEffects(EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU).isEmpty()) continue;

            UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            if (creatureControllerId == null) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    damagedPlayerId,
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect())),
                    amount,
                    creatureControllerId,
                    aura.getId(),
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            );
            entry.setDamageSourceCard(enchantedCreature.getCard());
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.abilityTriggers(aura.getCard()));
            log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU trigger fires ({} damage)",
                    gameData.id, aura.getCard().getName(), amount);
        }
    }

    // ── Controller-dealt-damage triggers (Living Artifact) ─────────────

    /**
     * Handles {@link EffectSlot#ON_CONTROLLER_DEALT_DAMAGE} — "Whenever you're dealt damage, ...".
     * Fires once per damage source (per the CR ruling that simultaneous sources trigger separately),
     * carrying only the amount so an {@code EventValue} amount ("put that many counters") can read it.
     * Scans the damaged player's own battlefield.
     * <p>
     * Also handles {@link EffectSlot#ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT} — "Whenever a source an
     * opponent controls deals damage to you, ..." (Retaliator Griffin) — but only when the damage
     * source is controlled by an opponent of the damaged player. {@code sourceControllerId} is the
     * controller of the damage source (the active player for combat damage to the defender, the
     * spell/ability's controller for non-combat damage); {@code null} disables the opponent-gated slot.
     */
    public void checkControllerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        if (amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount);
        boolean fromOpponent = sourceControllerId != null && !sourceControllerId.equals(damagedPlayerId);

        for (Permanent perm : List.copyOf(damagedPlayerBattlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE)) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                dispatch(match, EffectSlot.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);
            }
            if (fromOpponent) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT)) {
                    var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                    dispatch(match, EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);
                }
            }
        }
    }

    /**
     * Handles {@link EffectSlot#ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT} — "Whenever a source you
     * control deals damage to another player, ..." (Night Dealings). The outbound mirror of
     * {@link #checkControllerDealtDamageTriggers}: it scans the damage source's controller's own
     * battlefield, and only fires when the damaged player is someone else.
     */
    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        if (amount <= 0 || sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;

        List<Permanent> sourceControllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (sourceControllerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount);
        for (Permanent perm : List.copyOf(sourceControllerBattlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT)) {
                var match = new TriggerMatchContext(gameData, perm, sourceControllerId, effect);
                dispatch(match, EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect, ctx);
            }
        }
    }

    /**
     * Handles effects that trigger whenever an opponent is dealt damage, regardless of the damage
     * source's controller.
     */
    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId, int amount) {
        if (amount <= 0 || damagedPlayerId == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            if (watcherPlayerId.equals(damagedPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DEALT_DAMAGE)) {
                    var match = new TriggerMatchContext(gameData, perm, watcherPlayerId, effect);
                    registry.dispatch(match, EffectSlot.ON_OPPONENT_DEALT_DAMAGE, effect, ctx);
                }
            }
        });
    }

    /**
     * Handles {@link EffectSlot#ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT} — "Whenever a
     * creature of the chosen color deals damage to you or a white creature you control, ...".
     * Scans the damaged player's battlefield for watchers; the per-watcher chosen-color and
     * damaged-permanent filtering happens in the dispatched collector.
     *
     * @param damagedPlayerId  the damaged player, or the damaged permanent's controller
     * @param damagedPermanent the damaged permanent, or {@code null} when the player was damaged
     */
    public void checkCreatureDamageToYouOrYourPermanentTriggers(GameData gameData, UUID damagedPlayerId,
            Permanent damagedPermanent, Permanent damageSource, int damage) {
        if (damage <= 0 || damagedPlayerId == null || damageSource == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureDamageToYouOrYourPermanent(
                damageSource, damagedPlayerId, damagedPermanent, damage);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT)) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                dispatch(match, EffectSlot.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT, effect, ctx);
            }
        }
    }

    // ── Any-source-deals-damage triggers (Justice) ─────────────────────

    /**
     * Handles {@link EffectSlot#ON_ANY_SOURCE_DEALS_DAMAGE} — "Whenever a [color] creature or spell
     * deals damage, ...". Scans every battlefield for permanents with this slot and dispatches the
     * batched damage event (already summed across simultaneous targets) so each watcher can react
     * once. Callers pass the single summed total per source per damage event.
     */
    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage) {
        if (sourceCard == null || sourceControllerId == null || totalDamage <= 0) return;

        var ctx = new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId, totalDamage);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE)) {
                    var match = new TriggerMatchContext(gameData, perm, watcherPlayerId, effect);
                    dispatch(match, EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, effect, ctx);
                }
            }
        });

        // Blaze Commando: "whenever an instant or sorcery spell you control deals damage" — only the
        // spell's controller's battlefield watches, and only instant/sorcery sources qualify.
        if (sourceCard.hasType(CardType.INSTANT) || sourceCard.hasType(CardType.SORCERY)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield != null) {
                for (Permanent perm : List.copyOf(battlefield)) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE)) {
                        var match = new TriggerMatchContext(gameData, perm, sourceControllerId, effect);
                        dispatch(match, EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE, effect, ctx);
                    }
                }
            }
        }

        // Self triggers (El-Hajjâj): only the damage source's own "whenever this creature deals
        // damage" abilities fire. Keyed off the source card (not a battlefield scan) so it still
        // triggers when the source died dealing that damage.
        List<CardEffect> selfEffects = new ArrayList<>(sourceCard.getEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
        // Granted "whenever this creature deals damage" abilities live on the permanent, not the card
        // (the Genju cycle grants one to the animated land until end of turn).
        Permanent sourcePermanent = findPermanentByCardId(gameData, sourceCard.getId());
        if (sourcePermanent != null) {
            selfEffects.addAll(sourcePermanent.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
            selfEffects.addAll(sourcePermanent.getPersistentTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
        }
        for (CardEffect effect : selfEffects) {
            var match = new TriggerMatchContext(gameData, sourcePermanent, sourceControllerId, effect);
            dispatch(match, EffectSlot.ON_SELF_DEALS_DAMAGE, effect, ctx);
        }

        for (DelayedWatchedCreatureDealsDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealsDamage.class)) {
            if (!watch.watchedPermanentId().equals(sourcePermanentId)) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    (UUID) null,
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(totalDamage);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    public void queueEnchantedCreatureDealsDamageTriggers(GameData gameData, Permanent sourceCreature,
                                                           int damageDealt) {
        if (sourceCreature == null || damageDealt <= 0) return;

        List<StackEntry> entries = new ArrayList<>();
        collectEnchantedCreatureDealsDamageTriggers(gameData, sourceCreature, damageDealt, entries);
        entries.forEach(gameData::enqueueTrigger);
    }

    public void collectEnchantedCreatureDealsDamageTriggers(GameData gameData, Permanent sourceCreature,
                                                             int damageDealt, List<StackEntry> entries) {
        if (sourceCreature == null || damageDealt <= 0 || entries == null) return;

        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            List<CardEffect> effects = aura.getCard().getEffects(EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE);
            if (effects.isEmpty()) return;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    auraControllerId,
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    auraControllerId,
                    aura.getId());
            entry.setEventValue(damageDealt);
            entries.add(entry);
        });
    }

    /** The battlefield permanent whose card has this id, or null once it has left the battlefield. */
    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        List<Permanent> found = new ArrayList<>(1);
        gameData.forEachPermanent((playerId, perm) -> {
            if (perm.getCard().getId().equals(cardId)) {
                found.add(perm);
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    // ── Land-tap triggers ──────────────────────────────────────────────

    /**
     * Dispatches combat-damage-only self triggers from the source card. The source permanent ID is
     * retained even when state-based actions removed the source before triggered abilities were put
     * on the stack, so effects that require the source to have survived can check it at resolution.
     */
    public void queueSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                      UUID sourceControllerId, UUID sourcePermanentId,
                                                      int totalDamage,
                                                      List<CardEffect> snapshottedSelfEffects) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) {
            return;
        }

        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage);
        List<CardEffect> selfEffects = new ArrayList<>(sourceCard.getEffects(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE));
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (snapshottedSelfEffects != null) {
            selfEffects.addAll(snapshottedSelfEffects);
        } else if (sourcePermanent != null) {
            selfEffects.addAll(sourcePermanent.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE));
            selfEffects.addAll(sourcePermanent.getPersistentTriggeredEffects(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE));
        }
        for (CardEffect effect : selfEffects) {
            var match = new TriggerMatchContext(gameData, sourcePermanent, sourceControllerId, effect);
            registry.dispatch(match, EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE, effect, ctx);
        }

        // "Whenever a creature you control deals combat damage" watchers (Five-Alarm Fire). Scanned on
        // the damage source's controller's battlefield only; the watcher itself needn't be a creature.
        if (!sourceCard.hasType(CardType.CREATURE)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        for (Permanent watcher : List.copyOf(battlefield)) {
            for (CardEffect effect : watcher.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE)) {
                var match = new TriggerMatchContext(gameData, watcher, sourceControllerId, effect);
                dispatch(match, EffectSlot.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE, effect, ctx);
            }
        }

        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            for (CardEffect effect : watcher.getCard().getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE)) {
                var match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                registry.dispatch(match, EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE, effect, ctx);
            }
        });
    }

    /**
     * Handles {@link EffectSlot#ON_SELF_TAPPED_FOR_MANA} — "Whenever you tap this permanent for
     * mana, …" (Zhur-Taa Druid). Only the tapped permanent's own card is scanned, and only the
     * mana-ability tap path calls this, so tapping to attack or an opponent's forced tap never
     * triggers it. The caller defers the queued trigger like every other mana-ability trigger
     * (CR 603.3).
     *
     * @param gameData        the current game state
     * @param tappedPermanent the permanent that was just tapped for mana
     * @param controllerId    the controller of that permanent (also the player who tapped it)
     */
    public void checkSelfTappedForManaTriggers(GameData gameData, Permanent tappedPermanent, UUID controllerId) {
        List<CardEffect> effects = tappedPermanent.getCard().getEffects(EffectSlot.ON_SELF_TAPPED_FOR_MANA);
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                tappedPermanent.getCard(),
                controllerId,
                tappedPermanent.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                tappedPermanent.getId());
        entry.setTriggeringPermanentId(tappedPermanent.getId());
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(tappedPermanent.getCard()));
        log.info("Game {} - {} triggers on being tapped for mana", gameData.id, tappedPermanent.getCard().getName());
    }

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        // Desolation et al.: track who tapped a land for mana this turn even if no land-tap
        // trigger permanent is currently on the battlefield (2004-10-04 ruling).
        gameData.playersWhoTappedLandForManaThisTurn.add(tappingPlayerId);

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LandTap(tappingPlayerId, tappedLandId);

        // Snapshot each battlefield: a land-tap trigger may return the tapped land to hand
        // (Storm Cauldron), mutating the list we would otherwise be iterating.
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (dispatch(match, EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        applyTurnScopedExtraLandTapMana(gameData, tappingPlayerId, tappedLandId);

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * Applies {@code GameData.extraManaOnLandSubtypeTapThisTurn} — a turn-scoped, symmetric
     * "whenever a player taps a land of this subtype for mana, that player adds an additional
     * {X}" granted by a resolved effect rather than a permanent's static ability (Chaos Moon's
     * odd branch). Like the static land-tap triggers above it is a triggered mana ability, so it
     * pays straight into the tapping player's pool without using the stack.
     */
    private void applyTurnScopedExtraLandTapMana(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        if (gameData.extraManaOnLandSubtypeTapThisTurn.isEmpty()) {
            return;
        }
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, tappedLandId);
        if (tappedLand == null) {
            return;
        }
        Set<CardSubtype> types = gameQueryService.effectiveBasicLandTypes(gameData, tappedLand);
        ManaPool pool = gameData.playerManaPools.get(tappingPlayerId);
        if (pool == null) {
            return;
        }
        for (var entry : gameData.extraManaOnLandSubtypeTapThisTurn.entrySet()) {
            if (!types.contains(entry.getKey())) {
                continue;
            }
            pool.add(entry.getValue());
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(tappingPlayerId) + " adds 1 additional "
                            + entry.getValue().getCode() + " mana.")
                    .build());
        }
    }

    // ── Permanent-returned-to-hand triggers ────────────────────────────

    /**
     * Handles {@link EffectSlot#ON_ANY_PERMANENT_RETURNED_TO_HAND} — "Whenever a permanent is returned
     * to a player's hand, ...". Scans every battlefield for permanents with this slot and queues one
     * triggered ability per matching permanent, with {@code returnedToPlayerId} (the owner the permanent
     * was returned to) set as the non-targeting {@code targetId} so player-directed effects act on
     * "that player". Used by Warped Devotion.
     */
    public void checkPermanentReturnedToHandTriggers(GameData gameData, UUID returnedToPlayerId) {
        if (returnedToPlayerId == null) return;

        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_RETURNED_TO_HAND);
                if (effects.isEmpty()) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        returnedToPlayerId,
                        perm.getId());
                // "That player" is the owner the permanent returned to — determined by the event, not chosen.
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} permanent-returned-to-hand trigger pushed onto stack",
                        gameData.id, perm.getCard().getName());
            }
        }
    }

    // ── Ally-permanent-sacrificed triggers ──────────────────────────────

    public void checkAllyPermanentSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
        if (battlefield != null) {
            var ctx = new TriggerContext.AllySacrificed(sacrificingPlayerId, sacrificedCard);

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    var match = new TriggerMatchContext(gameData, perm, sacrificingPlayerId, effect);
                    dispatch(match, EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);
                }
            }
        }

        // Fire the global "whenever a player sacrifices a creature" watchers for the
        // sacrifice-self / sacrifice-as-cost paths that funnel through this method.
        checkAnyCreatureSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);

        // "When you sacrifice this" — the sacrificed card's own sacrifice-only death triggers
        collectSelfSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Fires {@link EffectSlot#ON_ANY_CREATURE_SACRIFICED} global watchers ("Whenever a player
     * sacrifices a creature", e.g. Thraximundar). Scans every battlefield, once per sacrificed
     * creature (creature-ness decided by last-known info on {@code sacrificedCard}); the trigger
     * belongs to the scanning permanent's own controller. Called from the two sacrifice choke
     * points — {@code DestructionSupport.sacrificeAndLog} (edict / chosen sacrifices) and
     * {@link #checkAllyPermanentSacrificedTriggers} (sacrifice-self / sacrifice-as-cost) — which are
     * mutually exclusive per event, so a single sacrifice never double-fires. Queues only; the may
     * abilities are drained by the caller / main resolution loop.
     */
    public void checkAnyCreatureSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        if (sacrificedCard == null || !sacrificedCard.hasType(CardType.CREATURE)) return;

        var ctx = new TriggerContext.AllySacrificed(sacrificingPlayerId, sacrificedCard);

        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_SACRIFICED);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
                    dispatch(match, EffectSlot.ON_ANY_CREATURE_SACRIFICED, effect, ctx);
                }
            }
        }
    }

    /**
     * Collects ON_DEATH effects that only trigger when the permanent was sacrificed
     * ({@link CardEffect#onlyTriggersOnSacrifice()}). Called from the sacrifice path after
     * the permanent has already left the battlefield.
     */
    private void collectSelfSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        if (sacrificedCard == null) return;
        List<CardEffect> deathEffects = sacrificedCard.getEffects(EffectSlot.ON_DEATH);
        if (deathEffects == null || deathEffects.isEmpty()) return;

        boolean wasCreature = sacrificedCard.hasType(CardType.CREATURE);
        var ctx = new TriggerContext.SelfDeath(sacrificedCard, sacrificingPlayerId, wasCreature, null);
        Permanent perm = new Permanent(sacrificedCard);
        for (CardEffect effect : deathEffects) {
            if (!effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, sacrificedCard, null, gameData, sacrificingPlayerId);
            if (resolvedEffect == null) continue;
            var match = new TriggerMatchContext(gameData, perm, sacrificingPlayerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
    }

    // ── Becomes-target-of-spell triggers ───────────────────────────────

    public void checkBecomesTargetOfSpellTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry spellEntry = null;
        for (int i = gameData.stack.size() - 1; i >= 0; i--) {
            StackEntry candidate = gameData.stack.get(i);
            if (candidate.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                    && candidate.getEntryType() != StackEntryType.ACTIVATED_ABILITY) {
                spellEntry = candidate;
                break;
            }
        }
        if (spellEntry == null) return;
        checkBecomesTargetOfSpellTriggers(gameData, spellEntry);
        checkTargetChoiceTriggers(gameData, spellEntry);
    }

    public void checkBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        List<UUID> targetIds = new ArrayList<>();
        if (spellEntry.getTargetId() != null
                && spellEntry.getTargetZone() == null) {
            targetIds.add(spellEntry.getTargetId());
        }
        if (spellEntry.getTargetIds() != null) {
            targetIds.addAll(spellEntry.getTargetIds());
        }

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetControllersWithAllyTrigger = new HashSet<>();

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetControllersWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(gameData, targetId, spellEntry);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, spellEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
            if (controllerId == null) continue;

            if (targetControllersWithAllyTrigger.add(controllerId)) {
                collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(gameData, controllerId, spellEntry);
            }
            collectAllyCreatureBecomesTargetOfOpponentTriggers(gameData, targetPermanent, controllerId, spellEntry);
            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent);
            collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfSpellTriggers(gameData, targetPermanent, controllerId);
            collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(gameData, targetPermanent, controllerId, spellEntry);
            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead — their triggers
            // monitor the enchanted/equipped creature, not themselves (Spectral Prison is not
            // sacrificed when a spell targets the Aura rather than the enchanted creature).
            if (!targetPermanent.isAttached()) {
                collectBecomesTargetTriggers(gameData, targetPermanent, controllerId, targetPermanent, spellEntry);
                collectBecomesTargetOfAuraSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentCounterTriggers(gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfSpellOrAbilityTriggers(gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                        gameData, targetPermanent, controllerId, spellEntry.getControllerId());
            }

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3a: the triggered ability is controlled by the controller of the
                        // permanent that has it (the aura/equipment), not the enchanted creature —
                        // the two differ when an Aura like Spectral Prison enchants an opponent's creature.
                        collectBecomesTargetTriggers(gameData, attached, playerId, targetPermanent, spellEntry);
                        collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, spellEntry);
                        collectBecomesTargetOfSpellOrAbilityTriggers(gameData, attached, playerId, spellEntry);
                    }
                }
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class) && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextETBTokenMultiTargetTrigger(gameData);
        }
    }

    /** Drains pending multi-target trigger choices (ETB token copies and ON_SELF_CAST multi-target). */
    public void processNextETBTokenMultiTargetTrigger(GameData gameData) {
        etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
    }

    /**
     * Checks becomes-target triggers for activated/triggered abilities that target permanents.
     * Must be called after an ability is pushed onto the stack with a target permanent.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry abilityEntry = gameData.stack.getLast();
        checkBecomesTargetOfAbilityTriggers(gameData, abilityEntry);
        checkTargetChoiceTriggers(gameData, abilityEntry);
    }

    /**
     * Checks becomes-target triggers for an explicitly supplied activated or triggered ability.
     * This overload is used when the ability is not the current top stack entry, such as while
     * Psychic Battle is resolving.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData, StackEntry abilityEntry) {
        if (abilityEntry == null) return;
        List<UUID> targetIds = new ArrayList<>();
        if (abilityEntry.getTargetId() != null
                && abilityEntry.getTargetZone() == null
                && !abilityEntry.isNonTargeting()) {
            targetIds.add(abilityEntry.getTargetId());
        }
        if (abilityEntry.getTargetIds() != null) {
            targetIds.addAll(abilityEntry.getTargetIds());
        }

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetControllersWithAllyTrigger = new HashSet<>();

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetControllersWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(gameData, targetId, abilityEntry);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, abilityEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());

            if (controllerId != null) {
                if (targetControllersWithAllyTrigger.add(controllerId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(gameData, controllerId, abilityEntry);
                }
            }

            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead.
            if (!targetPermanent.isAttached() && controllerId != null) {
                collectBecomesTargetOfSpellOrAbilityTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectBecomesTargetOfOpponentCounterTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                        gameData, targetPermanent, controllerId, abilityEntry.getControllerId());
            }

            // Check for "whenever a creature you control becomes the target of opponent's spell or ability"
            if (controllerId != null) {
                collectAllyCreatureBecomesTargetOfOpponentTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, abilityEntry);
            }

            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent);

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3b: triggered ability controlled by the aura/equipment's controller
                        collectBecomesTargetOfSpellOrAbilityTriggers(gameData, attached, playerId, abilityEntry);
                        collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, abilityEntry);
                    }
                }
            }
        }
    }

    /**
     * Checks all permanents controlled by the targeted player for effects that watch that player or
     * one of their permanents becoming the target of an opponent's spell or ability.
     */
    private void collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    targetControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    triggeringEntry.getControllerId(),
                    source.getId()
            );
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-permanent-or-player-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    public void checkTargetChoiceTriggers(GameData gameData, StackEntry targetEntry) {
        if (targetEntry == null || !targetEntry.hasAnyTarget() || targetEntry.isNonTargeting()) {
            return;
        }

        gameData.forEachPermanent((controllerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_CHOOSES_TARGETS)) {
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        permanent.getCard(),
                        controllerId,
                        permanent.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        permanent.getId()
                );
                trigger.setTriggeringCardId(targetEntry.getCard().getId());
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers when targets are chosen", gameData.id,
                        permanent.getCard().getName());
            }
        });
    }

    private void collectBecomesTargetTriggers(GameData gameData, Permanent source, UUID controllerId,
                                              Permanent targetedCreature, StackEntry spellEntry) {
        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL);
        if (effects.isEmpty()) return;

        // Split targeting effects (which choose "any target" at resolution — Livewire Lash's damage)
        // from non-targeting ones (which resolve against the source permanent — an Illusion's
        // "sacrifice it"). The latter must go straight on the stack carrying sourcePermanentId; the
        // any-target interaction never records a source, so a self-referential effect would no-op.
        List<CardEffect> targetingEffects = new ArrayList<>();
        List<CardEffect> nonTargetingEffects = new ArrayList<>();
        List<CardEffect> triggeringSpellEffects = new ArrayList<>();
        for (CardEffect effect : effects) {
            if (effect instanceof TriggeringSpellReferencingEffect) {
                triggeringSpellEffects.add(effect);
            } else if (effect.targetSpec().declaredTarget() == null) {
                nonTargetingEffects.add(effect);
            } else {
                targetingEffects.add(effect);
            }
        }

        if (!nonTargetingEffects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(nonTargetingEffects),
                    null,
                    source.getId()
            );
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued", gameData.id, source.getCard().getName());
        }

        if (!triggeringSpellEffects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(triggeringSpellEffects),
                    spellEntry.getCard().getId(),
                    Zone.STACK
            );
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued against the targeting spell", gameData.id, source.getCard().getName());
        }

        if (!targetingEffects.isEmpty()) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    targetedCreature.getCard(), controllerId, new ArrayList<>(targetingEffects)
            ));

            gameLogService.append(gameData, GameLog.cardThen(targetedCreature.getCard(), "'s triggered ability triggers — choose a target for damage."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued", gameData.id, targetedCreature.getCard().getName());
        }
    }

    private void collectBecomesTargetOfSpellOrAbilityTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        List<CardEffect> effects = new ArrayList<>(
                source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY));
        // Dismiss into Dream continuously grants "When this creature becomes the target of a spell
        // or ability, sacrifice it" to each creature its controller's opponents control.
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, source, EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY));
        // Makeshift Mannequin: while a permanent has a mannequin counter, it has "When this creature
        // becomes the target of a spell or ability, sacrifice it."
        if (source.getCounterCount(CounterType.MANNEQUIN) > 0) {
            effects.add(new SacrificeSelfEffect());
        }
        if (effects.isEmpty()) return;

        // Glyph Keeper: "Whenever this creature becomes the target of a spell or ability for the first
        // time each turn, counter that spell or ability." A counterspelling effect in this slot fires at
        // most once per turn per permanent and counters the object that triggered it — set as the target
        // in the STACK zone so CounterSpellEffect finds the triggering entry sitting below this trigger.
        if (effects.stream().anyMatch(CounterSpellingEffect.class::isInstance)) {
            if (source.isBecomeTargetCounterUsedThisTurn()) return;
            source.setBecomeTargetCounterUsedThisTurn(true);

            StackEntry counterEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    0,
                    triggeringEntry.getCard().getId(),
                    source.getId(),
                    null,
                    Zone.STACK,
                    null,
                    null
            );
            gameData.stack.add(counterEntry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers — counter that spell or ability."));
            log.info("Game {} - {} becomes-target-of-spell-or-ability counter trigger queued",
                    gameData.id, source.getCard().getName());
            return;
        }

        if (effects.stream().anyMatch(TriggeringSpellReferencingEffect.class::isInstance)) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    0,
                    triggeringEntry.getCard().getId(),
                    source.getId(),
                    null,
                    Zone.STACK,
                    null,
                    null
            );
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued against the triggering object",
                    gameData.id, source.getCard().getName());
            return;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued", gameData.id, source.getCard().getName());
    }

    /**
     * "Whenever this creature becomes the target of an Aura spell, &lt;effect&gt;." Narrower than
     * {@link EffectSlot#ON_BECOMES_TARGET_OF_SPELL}: only Aura spells count, and there is no controller
     * restriction, so an opponent's Aura triggers it just as the controller's own does. Used by
     * Fugitive Druid.
     */
    private void collectBecomesTargetOfAuraSpellTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry spellEntry) {
        if (spellEntry.getCard() == null || !spellEntry.getCard().isAura()) return;

        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_AURA_SPELL);
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-Aura-spell trigger queued", gameData.id, source.getCard().getName());
    }

    private void collectBecomesTargetOfOpponentCounterTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(triggeringEntry.getControllerId())) return;

        List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, source, EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL));
        if (effects.isEmpty()) return;

        for (CardEffect effect : effects) {
            if (effect instanceof CounterUnlessEffect counterEffect) {
                // Put the counter-unless effect directly on the stack targeting the spell. The pay
                // and discard variants queue an identical trigger entry; only the log wording differs
                // by the kind of ransom demanded.
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(counterEffect)),
                        triggeringEntry.getCard().getId(),
                        Zone.STACK
                );
                gameData.stack.add(entry);

                switch (counterEffect.ransomKind()) {
                    case PAY_MANA -> {
                        String paymentText = counterEffect instanceof CounterUnlessPaysEffect pays
                                && pays.amount() == 0 && pays.lifeCost() > 0
                                ? pays.lifeCost() + " life"
                                : "{" + counterEffect.ransomMagnitude() + "}";
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers — counter unless controller pays "
                                + paymentText + "."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter trigger queued", gameData.id, source.getCard().getName());
                    }
                    case DISCARD_CARD -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers — counter unless controller discards a card."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-discard trigger queued", gameData.id, source.getCard().getName());
                    }
                }
            }
        }
    }

    private void collectControllerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID playerId, StackEntry triggeringEntry) {
        if (playerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            for (CardEffect effect : source.getCard().getEffects(
                    EffectSlot.ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY)) {
                if (!(effect instanceof CounterUnlessEffect)) continue;

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        playerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(List.of(effect)),
                        triggeringEntry.getCard().getId(),
                        Zone.STACK
                );
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        "'s triggered ability triggers — counter unless controller pays {1}."));
                log.info("Game {} - {} controller-target counter trigger queued", gameData.id,
                        source.getCard().getName());
            }
        }
    }

    /**
     * "Whenever this creature becomes the target of a spell or ability an opponent controls, &lt;non-counter effect&gt;."
     * Handles the non-counter effects in {@link EffectSlot#ON_BECOMES_TARGET_OF_OPPONENT_SPELL} (e.g. "you may
     * draw a card"). Counter/Ward effects on that slot are handled separately by
     * {@link #collectBecomesTargetOfOpponentCounterTriggers}. Called from both the spell and
     * ability paths so the "or ability" clause is honored. Used by Tenured Concocter.
     */
    private void collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
            GameData gameData, Permanent source, UUID controllerId, UUID spellOrAbilityControllerId) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(spellOrAbilityControllerId)) return;

        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL).stream()
                .filter(e -> !(e instanceof CounterUnlessEffect))
                .toList();
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-opponent-spell-or-ability (non-counter) trigger queued",
                gameData.id, source.getCard().getName());
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link EffectSlot#ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY}.
     * Only fires when the targeted permanent is a creature and the spell/ability
     * is controlled by an opponent of the creature's controller.
     */
    private void collectAllyCreatureBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        // Only trigger for creatures
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        // Only trigger if the spell/ability is controlled by an opponent
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            if (effects.isEmpty()) continue;

            List<CardEffect> nonCounterEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, source, creatureControllerId, targetPermanent, effect);
                if (resolved == null) continue;

                if (resolved instanceof CounterSpellingEffect) {
                    StackEntry counterTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(),
                            creatureControllerId,
                            source.getCard().getName() + "'s triggered ability",
                            List.of(resolved),
                            triggeringEntry.getCard().getId(),
                            Zone.STACK);
                    gameData.stack.add(counterTrigger);
                    gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                            "'s triggered ability triggers."));
                } else {
                    nonCounterEffects.add(resolved);
                }
            }

            if (!nonCounterEffects.isEmpty()) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        creatureControllerId,
                        source.getCard().getName() + "'s triggered ability",
                        nonCounterEffects,
                        null,
                        source.getId()
                );
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            }
            log.info("Game {} - {} ally-creature-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link EffectSlot#ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY}. Only fires when the
     * targeted permanent is a creature and the targeting spell is an instant or a sorcery; there is
     * no controller restriction, so the creature's controller's own spells trigger it too. The
     * targeted creature is stored as the non-targeting {@code targetId} so the resolved effect can
     * act on it. Used by Wild Defiance.
     */
    private void collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry spellEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        if (!spellEntry.getCard().hasType(CardType.INSTANT) && !spellEntry.getCard().hasType(CardType.SORCERY)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY);
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    targetPermanent.getId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-creature-becomes-target-of-instant-or-sorcery trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    public void checkCreatureTapForManaTriggers(GameData gameData, UUID tappingPlayerId,
                                                UUID tappedCreatureId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(tappingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureTapForMana(tappingPlayerId, tappedCreatureId);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(
                    EffectSlot.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA)) {
                var match = new TriggerMatchContext(gameData, perm, tappingPlayerId, effect);
                registry.dispatch(match, EffectSlot.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA, effect, ctx);
            }
        }
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link EffectSlot#ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL}. Only the spell path calls this
     * method, so activated abilities do not trigger it. The card's target groups are reused to
     * choose any optional targets as the triggered ability is put on the stack.
     */
    private void collectAllyCreatureBecomesTargetOfSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL));
            if (effects.isEmpty()) continue;

            if (source.getCard().getSpellTargets().size() > 1
                    || etbTokenTargetService.needsSlotBySlotTargetSelection(source.getCard())) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        source.getCard(), creatureControllerId, effects, source.getId(), List.of(), 0, 0));
            } else {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                        source.getCard(), creatureControllerId, effects, source.getId(),
                        source.getCard().getTargetFilter()));
            }

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers — choose targets."));
            log.info("Game {} - {} ally-creature-becomes-target-of-spell trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks ALL permanents across every battlefield for
     * {@link EffectSlot#ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY}. Only fires when the
     * targeted permanent is a creature. The targeted creature is stored as the non-targeting
     * {@code targetId} so the resolved effect can act on it. Used by Cowardice.
     */
    private void collectAnyCreatureBecomesTargetTriggers(GameData gameData, Permanent targetPermanent) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent source : battlefield) {
                List<CardEffect> effects = source.getCard().getEffects(
                        EffectSlot.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY);
                if (effects.isEmpty()) continue;

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        playerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(effects),
                        targetPermanent.getId(),
                        source.getId()
                );
                entry.setNonTargeting(true);
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
                log.info("Game {} - {} any-creature-becomes-target trigger queued",
                        gameData.id, source.getCard().getName());
            }
        }
    }

    /**
     * Checks the spell/ability controller's battlefield for
     * {@link EffectSlot#ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY}. Only fires
     * when the targeted permanent is a creature controlled by an opponent of the spell/ability's
     * controller. The targeted creature is stored as the non-targeting {@code targetId} and the
     * listening permanent as the {@code sourcePermanentId}. Used by Willbreaker.
     */
    private void collectOpponentCreatureBecomesTargetOfYourSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;

        UUID triggeringControllerId = triggeringEntry.getControllerId();
        if (triggeringControllerId == null || triggeringControllerId.equals(creatureControllerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(triggeringControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = source.getCard().getEffects(
                    EffectSlot.ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY);
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    triggeringControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    targetPermanent.getId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} opponent-creature-becomes-target-of-your-spell trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId) {
        if (damageDealt > 0) {
            checkEnchantedCreatureDealtDamageTriggers(gameData, damagedCreature, damageDealt);
        }

        List<CardEffect> effects = damagedCreature.getCard().getEffects(EffectSlot.ON_DEALT_DAMAGE);
        if (effects.isEmpty()) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, damageSourceControllerId);

        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, damagedCreature, controllerId, effect);
            dispatch(match, EffectSlot.ON_DEALT_DAMAGE, effect, ctx);
        }
    }

    // ── Enchanted-creature-dealt-damage triggers ───────────────────────

    public void checkEnchantedCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature, int damageDealt) {
        if (damageDealt <= 0) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, null);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(damagedCreature.getId())) {
                return;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)) {
                var match = new TriggerMatchContext(gameData, perm, auraOwnerId, effect);
                dispatch(match, EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);
            }
        });
    }

    // ── Opponent-creature-dealt-damage triggers ──────────────────────────

    /**
     * Fires ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers on permanents whose controller
     * is different from the damaged creature's controller (i.e. the damaged creature is
     * an opponent's creature from the perspective of the permanent's controller).
     * Called once per damaged creature — each call produces one trigger per listening permanent.
     */
    public void checkOpponentCreatureDealtDamageTriggers(GameData gameData, UUID damagedCreatureControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            // Only fire when the damaged creature was controlled by an opponent of this permanent's controller
            if (playerId.equals(damagedCreatureControllerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_DEALT_DAMAGE);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (opponent creature dealt damage)", gameData.id, perm.getCard().getName());
            }
        });
    }

    // ── Ally-creature-deals-damage-to-creature reflection (Greatbow Doyen) ──

    /**
     * Fires ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE triggers. When a creature the watcher's
     * controller controls (matching the effect's source filter) deals damage to a creature, the
     * damage-source creature deals that much damage to the damaged creature's controller. Called
     * once per source/target/damage event; {@code combatDamage} tells the combat-only listeners
     * (Sosuke, Son of Seshiro) whether this event was combat damage.
     */
    public void checkAllyDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId, int damage,
            boolean combatDamage) {
        if (damageSource == null || damageSourceControllerId == null || damagedCreatureControllerId == null || damage <= 0) {
            return;
        }

        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            // "a creature you control" — the damage source must be controlled by the watcher's controller.
            if (!watcherControllerId.equals(damageSourceControllerId)) return;
            // The damage source watches itself below, whether or not it survived the damage.
            if (watcher.getId().equals(damageSource.getId())) return;

            fireAllyDealtDamageToCreatureTrigger(gameData, watcher, damageSource, damageSourceControllerId,
                    damagedCreatureControllerId, damagedCreatureId, damage, combatDamage);
        });

        // A self-scoped trigger ("Whenever this creature deals damage to a creature, …") triggered
        // when the damage was dealt, so it still goes on the stack when lethal damage back from the
        // blocker has already moved the source off the battlefield.
        fireAllyDealtDamageToCreatureTrigger(gameData, damageSource, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damage, combatDamage);
    }

    private void fireAllyDealtDamageToCreatureTrigger(GameData gameData, Permanent watcher, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId, int damage,
            boolean combatDamage) {
        List<CardEffect> effects = new ArrayList<>(
                watcher.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE));
        // Abilities granted until end of turn (Cruel Deceiver) live on the permanent, not the card.
        effects.addAll(watcher.getTemporaryTriggeredEffects(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE));

        TriggerContext context = new TriggerContext.CreatureDealsDamageToCreature(
                damageSource, damagedCreatureId, damage, combatDamage);

        for (CardEffect effect : effects) {
            TriggerMatchContext match = new TriggerMatchContext(gameData, watcher, damageSourceControllerId, effect);
            if (registry.dispatch(match, EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE, effect, context)) {
                continue;
            }
            if (effect instanceof ReflectAllyDamageToDamagedCreatureControllerEffect reflect) {
                if (reflect.combatOnly() && !combatDamage) continue;
                if (reflect.sourceMustBeWatcher() && !watcher.getId().equals(damageSource.getId())) continue;
                if (!reflect.sourceMustBeWatcher() && reflect.sourceFilter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, damageSource, reflect.sourceFilter())) {
                    continue;
                }

                // The normal form deals damage; Flayed Nim's self-scoped form causes life loss.
                CardEffect triggeredEffect = reflect.lifeLoss()
                        ? new LoseLifeEffect(damage, LoseLifeRecipient.TARGET_PLAYER)
                        : new DealDamageToPlayersEffect(damage, DamageRecipient.TARGET_PLAYER);
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(triggeredEffect)),
                        damagedCreatureControllerId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                if (reflect.lifeLoss()) {
                    log.info("Game {} - {} causes {} life loss to {}", gameData.id,
                            watcher.getCard().getName(), damage,
                            gameData.playerIdToName.get(damagedCreatureControllerId));
                } else {
                    log.info("Game {} - {} reflects {} damage to {}", gameData.id,
                            watcher.getCard().getName(), damage,
                            gameData.playerIdToName.get(damagedCreatureControllerId));
                }
            } else if (effect instanceof DamageDamagedCreatureControllerAndSelfEffect punisher) {
                // "this creature" — fire only when the watcher itself dealt the damage.
                if (!watcher.getId().equals(damageSource.getId())) continue;

                // This creature deals N damage to that creature's controller and M damage to you.
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new DealDamageToPlayersEffect(punisher.amountToDamagedCreatureController(), DamageRecipient.TARGET_PLAYER),
                                new DealDamageToPlayersEffect(punisher.amountToSelf(), DamageRecipient.CONTROLLER))),
                        damagedCreatureControllerId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} deals {} to {} and {} to its controller", gameData.id,
                        watcher.getCard().getName(), punisher.amountToDamagedCreatureController(),
                        gameData.playerIdToName.get(damagedCreatureControllerId), punisher.amountToSelf());
            } else if (effect instanceof DamagedCreatureTriggerEffect damagedCreatureTrigger) {
                Permanent triggerSource = damageSource;
                if (damagedCreatureTrigger.equipmentScoped()) {
                    if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                    triggerSource = watcher;
                } else if (!watcher.getId().equals(damageSource.getId())) {
                    // "this creature" — fire only when the watcher itself dealt the damage.
                    continue;
                }
                if (damagedCreatureId == null) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        triggerSource.getCard(),
                        damageSourceControllerId,
                        triggerSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(damagedCreatureTrigger.triggeredEffect())),
                        damagedCreatureId,
                        triggerSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} will resolve its damaged-creature trigger", gameData.id,
                        watcher.getCard().getName());
            } else if (effect instanceof TapAndSkipUntapDamagedCreatureEffect) {
                // "this creature" — fire only when the watcher itself dealt the damage.
                if (!watcher.getId().equals(damageSource.getId())) continue;
                if (damagedCreatureId == null) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                new SkipNextUntapEffect(TapUntapScope.TARGET))),
                        damagedCreatureId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} taps and locks the creature it damaged", gameData.id,
                        watcher.getCard().getName());
            } else if (effect instanceof EquipmentTapsAndLocksDamagedCreatureEffect) {
                // "equipped creature" — the watcher is the Equipment, so it must be attached to
                // the creature that dealt the damage. Last-known attachment is valid when that
                // creature died to the same damage event.
                if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                if (damagedCreatureId == null) continue;

                UUID watcherControllerId = gameData.findControllerOf(watcher);
                if (watcherControllerId == null) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        watcherControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                DoesntUntapEffect.targetWhileSourceOnBattlefield())),
                        damagedCreatureId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} taps and locks the creature damaged by its equipped creature",
                        gameData.id, watcher.getCard().getName());
            } else if (effect instanceof DestroyDamagedCreatureAtEndOfCombatEffect delayedDestroy) {
                if (delayedDestroy.combatDamageOnly() && !combatDamage) continue;
                if (delayedDestroy.selfOnly() && !watcher.getId().equals(damageSource.getId())) continue;
                if (damagedCreatureId == null) continue;
                if (delayedDestroy.sourceFilter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        damageSource,
                        delayedDestroy.sourceFilter(),
                        FilterContext.of(gameData).withSourcePermanentSnapshot(watcher))) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        damageSourceControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new DestroyTargetPermanentAtEndOfCombatEffect())),
                        damagedCreatureId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} schedules the damaged creature for destruction at end of combat",
                        gameData.id, watcher.getCard().getName());
            } else if (effect instanceof EquipmentDamagesOtherDefendingCreaturesEffect) {
                // "equipped creature" — the watcher is the Equipment, so it must be attached to
                // the creature that dealt the damage. The ability triggered when the damage was
                // dealt, so a host that has already died to the same combat damage (which detaches
                // the Equipment) still counts, read from last-known attachment.
                if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                if (damagedCreatureId == null) continue;
                if (!wasBlocking(gameData, damageSource, damagedCreatureId, combatDamage)) continue;

                // The Equipment deals that much damage to each other creature defending player controls.
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        damageSourceControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new DealDamageToEachMatchingPermanentEffect(damage,
                                new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(damagedCreatureId)),
                                EachPermanentScope.TARGET_PLAYER))),
                        damagedCreatureControllerId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} deals {} damage to each other creature {} controls", gameData.id,
                        watcher.getCard().getName(), damage,
                        gameData.playerIdToName.get(damagedCreatureControllerId));
            }
        }
    }

    /**
     * Whether the creature that was just dealt damage was a blocking creature. Lethal damage may
     * already have moved it off the battlefield, in which case the fallback reads the damage source:
     * a creature only deals combat damage to creatures blocking it while it is attacking.
     */
    private boolean isEquippedBy(GameData gameData, Permanent equipment, Permanent host) {
        return isEquippedBy(gameData, equipment, host.getId());
    }

    private boolean isEquippedBy(GameData gameData, Permanent equipment, UUID hostId) {
        if (hostId.equals(equipment.getAttachedTo())) return true;
        // Only a host that already left the battlefield may be matched on last-known attachment.
        return hostId.equals(equipment.getLastAttachedTo())
                && gameQueryService.findPermanentById(gameData, hostId) == null;
    }

    /**
     * Whether the creature that was just dealt damage was a blocking creature. For combat damage the
     * damage source answers it: a creature deals combat damage to creatures blocking it only while it
     * is attacking (a blocking creature damages the attacker instead). The damaged creature's own
     * blocking flag is unusable there — lethal damage may already have removed it from the
     * battlefield, and combat state is torn down when the creature it blocked dies.
     */
    private boolean wasBlocking(GameData gameData, Permanent damageSource, UUID damagedCreatureId, boolean combatDamage) {
        if (combatDamage) return damageSource.isAttacking();
        Permanent damaged = gameQueryService.findPermanentById(gameData, damagedCreatureId);
        return damaged != null && damaged.isBlocking();
    }

    // ── Any-creature-dealt-damage triggers ─────────────────────────────

    /**
     * Fires ON_ANY_CREATURE_DEALT_DAMAGE triggers on every permanent with that slot, regardless of
     * who controls the damaged creature. Called once per damaged creature.
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    int damageDealt) {
        UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        checkAnyCreatureDealtDamageTriggers(gameData, damagedCreature, damagedCreatureControllerId, damageDealt);
    }

    /**
     * Variant for combat, where the damaged creature may already have left the battlefield by the
     * time its trigger is queued and its controller was captured during damage processing.
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    UUID damagedCreatureControllerId, int damageDealt) {
        if (damagedCreatureControllerId == null || damageDealt <= 0) return;

        var ctx = new TriggerContext.AnyCreatureDealtDamage(
                damagedCreature, damagedCreatureControllerId, damageDealt);
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                registry.dispatch(match, EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE, effect, ctx);
            }
        });

        collectTemporaryGlobalTriggers(gameData, EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE,
                damagedCreature.getId(), damageDealt);
    }

    private void collectTemporaryGlobalTriggers(GameData gameData, EffectSlot slot, UUID targetId,
                                                int eventValue) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (watcher.slot() != slot) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(targetId);
            entry.setEventValue(eventValue);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global {} trigger fires",
                    gameData.id, watcher.sourceCard().getName(), slot.name());
        }
    }

    // ── Enchanted-permanent-tap triggers ───────────────────────────────

    public void checkEnchantedPermanentTapTriggers(GameData gameData, Permanent tappedPermanent) {
        UUID tappedPermanentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(tappedPermanent)) {
                tappedPermanentControllerId = pid;
                break;
            }
        }
        if (tappedPermanentControllerId == null) return;

        var ctx = new TriggerContext.EnchantedPermanentTap(tappedPermanent, tappedPermanentControllerId);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(tappedPermanent.getId())) {
                return;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)) {
                var match = new TriggerMatchContext(gameData, perm, auraOwnerId, effect);
                dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);
            }
        });

        // "Whenever a permanent you control becomes tapped" triggers (e.g. Judge of Currents).
        UUID controllerId = tappedPermanentControllerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED)) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(tappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                // Leave targetId null for may-target tap triggers (Surgespanner). Bake the tapped
                // permanent's controller only when damage needs TRIGGERING_PERMANENT_CONTROLLER
                // (Royal Decree). triggeringPermanentId always carries "it" (Freyalise's Winds).
                if (resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        && !(resolved instanceof MayEffect)
                        && !(resolved instanceof MayPayManaEffect)) {
                    gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), perm.getId(),
                            tappedPermanent.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on ally permanent tap ({}, awaiting target)",
                            gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
                    continue;
                }
                UUID bakedTargetId = bakeTriggeringPermanentControllerTarget(resolved, controllerId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        bakedTargetId,
                        perm.getId()
                );
                entry.setTriggeringPermanentId(tappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally permanent tap ({})",
                        gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
            }
        });

        // "Whenever a permanent an opponent controls becomes tapped" triggers (e.g. Thoughtleech).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(controllerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED)) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(tappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                UUID bakedTargetId = bakeTriggeringPermanentControllerTarget(resolved, controllerId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        bakedTargetId,
                        perm.getId()
                );
                entry.setTriggeringPermanentId(tappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on opponent permanent tap ({})",
                        gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
            }
        });
    }

    /**
     * For ally/opponent becomes-tapped slots: bake the tapped permanent's controller as
     * {@code targetId} only when the resolved effect deals damage to
     * {@link DamageRecipient#TRIGGERING_PERMANENT_CONTROLLER}. Other effects keep {@code null}
     * so may-target tap triggers (Surgespanner) still choose at resolution.
     */
    private static UUID bakeTriggeringPermanentControllerTarget(CardEffect resolved, UUID tappedControllerId) {
        if (resolved instanceof DealDamageToPlayersEffect damage
                && damage.recipient() == DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER) {
            return tappedControllerId;
        }
        return null;
    }

    // ── Becomes-untapped triggers ──────────────────────────────────────

    /**
     * "Whenever this permanent becomes untapped" triggers (e.g. Hollowsage). Called from the untap
     * call sites after a permanent transitions from tapped to untapped. Fires only on the permanent
     * that became untapped, queueing a non-targeting triggered ability whose {@code sourcePermanentId}
     * is that permanent; any player targeting for a wrapped {@code MayEffect} happens at resolution.
     */
    public void checkBecomesUntappedTriggers(GameData gameData, Permanent untappedPermanent) {
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(untappedPermanent)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) return;

        for (CardEffect effect : untappedPermanent.getCard().getEffects(EffectSlot.ON_SELF_BECOMES_UNTAPPED)) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    untappedPermanent.getCard(),
                    controllerId,
                    untappedPermanent.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    untappedPermanent.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(untappedPermanent.getCard()));
            log.info("Game {} - {} triggers on becoming untapped", gameData.id, untappedPermanent.getCard().getName());
        }

        // "Whenever a permanent you control becomes untapped" triggers (e.g. Wake Thrasher).
        UUID untappedControllerId = controllerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(untappedControllerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_PERMANENT_BECOMES_UNTAPPED)) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(untappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally permanent untap ({})",
                        gameData.id, perm.getCard().getName(), untappedPermanent.getCard().getName());
            }
        });

        gameData.forEachPermanent((ownerId, perm) -> {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_BECOMES_UNTAPPED)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                );
                entry.setEventPlayerIds(List.of(untappedControllerId));
                entry.setTriggeringPermanentId(untappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on any permanent untap ({})",
                        gameData.id, perm.getCard().getName(), untappedPermanent.getCard().getName());
            }
        });
    }

    /**
     * "When this creature becomes renowned" (Relic Seeker) and "whenever a creature you control
     * becomes renowned" (Valeron Wardens) triggers. Called from
     * {@code RenownEffectHandler} at the moment renown flips the creature from not-renowned to
     * renowned; a creature that was already renowned never reaches this point (CR 702.112c).
     *
     * @param gameData         the current game state to modify
     * @param renownedCreature the creature that just became renowned
     * @param controllerId     the player controlling it
     */
    public void checkBecomesRenownedTriggers(GameData gameData, Permanent renownedCreature, UUID controllerId) {
        for (CardEffect effect : renownedCreature.getCard().getEffects(EffectSlot.ON_SELF_BECOMES_RENOWNED)) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    renownedCreature.getCard(),
                    controllerId,
                    renownedCreature.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    renownedCreature.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(renownedCreature.getCard()));
            log.info("Game {} - {} triggers on becoming renowned", gameData.id, renownedCreature.getCard().getName());
        }

        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_BECOMES_RENOWNED)) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(renownedCreature, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally creature becoming renowned ({})",
                        gameData.id, perm.getCard().getName(), renownedCreature.getCard().getName());
            }
        });
    }

    /** Fires the newly monstrous permanent's own triggered abilities. */
    public void checkBecomesMonstrousTriggers(GameData gameData, Permanent monstrousPermanent,
                                               UUID controllerId) {
        checkBecomesMonstrousTriggers(gameData, monstrousPermanent, controllerId, 0);
    }

    public void checkBecomesMonstrousTriggers(GameData gameData, Permanent monstrousPermanent,
                                               UUID controllerId, int xValue) {
        TriggerContext.SelfBecomesMonstrous context =
                new TriggerContext.SelfBecomesMonstrous(controllerId, xValue);
        for (CardEffect effect : monstrousPermanent.getCard().getEffects(EffectSlot.ON_SELF_BECOMES_MONSTROUS)) {
            var match = new TriggerMatchContext(gameData, monstrousPermanent, controllerId, effect);
            registry.dispatch(match, EffectSlot.ON_SELF_BECOMES_MONSTROUS, effect, context);
        }
    }

    /**
     * "Whenever this permanent phases out" triggers (e.g. Teferi's Imp). Called from
     * {@code PhasingService} <em>before</em> the permanent leaves the battlefield: a phased-out
     * permanent is treated as though it does not exist (CR 702.26b), so these abilities look back in
     * time to the game state before it phased out (CR 603.10b).
     *
     * @param gameData     the current game state to modify
     * @param permanent    the permanent that is phasing out
     * @param controllerId the player controlling it as it phases out
     */
    public void checkPhasesOutTriggers(GameData gameData, Permanent permanent, UUID controllerId) {
        enqueuePhasingTriggers(gameData, permanent, controllerId, EffectSlot.ON_SELF_PHASES_OUT, "out");
    }

    /**
     * "Whenever this permanent phases in" triggers (e.g. Teferi's Imp). Called from
     * {@code PhasingService} after the permanent is back on its controller's battlefield
     * (CR 702.26c).
     *
     * @param gameData     the current game state to modify
     * @param permanent    the permanent that phased in
     * @param controllerId the player controlling it
     */
    public void checkPhasesInTriggers(GameData gameData, Permanent permanent, UUID controllerId) {
        enqueuePhasingTriggers(gameData, permanent, controllerId, EffectSlot.ON_SELF_PHASES_IN, "in");
    }

    private void enqueuePhasingTriggers(GameData gameData, Permanent permanent, UUID controllerId,
                                        EffectSlot slot, String direction) {
        for (CardEffect effect : permanent.getCard().getEffects(slot)) {
            // Targeted phase-in (Shimmering Efreet): choose the target when the ability is put on the
            // stack. Queued here during the untap-step phasing action and drained at upkeep start.
            if (slot == EffectSlot.ON_SELF_PHASES_IN
                    && effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                gameData.queueInteraction(new PermanentChoiceContext.PhasesInTriggerTarget(
                        permanent.getCard(), controllerId, new ArrayList<>(List.of(effect)), permanent.getId()));
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers on phasing {} (awaiting target)",
                        gameData.id, permanent.getCard().getName(), direction);
                continue;
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    permanent.getCard(),
                    controllerId,
                    permanent.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    permanent.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
            log.info("Game {} - {} triggers on phasing {}", gameData.id, permanent.getCard().getName(), direction);
        }
    }

    // ── Ability-activation triggers ────────────────────────────────────

    /**
     * "Whenever you activate an ability of {a permanent}" triggers (e.g. Ceaseless Searblades).
     * Fires on every permanent the activating player controls that has an
     * {@link EffectSlot#ON_CONTROLLER_ACTIVATES_ABILITY} effect, filtered (when wrapped in
     * {@link TriggeringPermanentConditionalEffect}) by the permanent whose ability was activated.
     */
    public void checkControllerActivatesAbilityTriggers(GameData gameData, UUID activatingPlayerId, Permanent activatedPermanent) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY)) {
                CardEffect resolved = resolveTriggeringPermanentConditional(gameData, perm, ownerId, activatedPermanent, effect);
                if (resolved == null) continue;
                if (resolved.targetSpec().declares(TargetPredicates.anyTarget())) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), false, null, 0, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} queues an any-target ability-activation trigger ({})",
                            gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
                    continue;
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ability activation ({})",
                        gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
            }
        });
    }

    /**
     * "Whenever you activate an eternalize or embalm ability" triggers (Vizier of the Anointed).
     * Fires once per activation on every permanent the activating player controls that has an
     * {@link EffectSlot#ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM} effect. Called from the
     * graveyard-ability activation path only when the activated ability is an embalm/eternalize
     * ability, so no per-permanent condition is needed.
     */
    public void checkControllerActivatesEternalizeOrEmbalmTriggers(GameData gameData, UUID activatingPlayerId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on eternalize/embalm activation",
                        gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * "Whenever an opponent activates an ability of {a permanent}, if it isn't a mana ability, ..."
     * triggers (e.g. Harsh Mentor). Fires on every permanent NOT controlled by the activating player
     * that has an {@link EffectSlot#ON_OPPONENT_ACTIVATES_NONMANA_ABILITY} effect, optionally filtered
     * (when wrapped in {@link TriggeringPermanentConditionalEffect}) by the permanent whose ability was
     * activated. Called only from the non-mana activation path, so mana abilities never trigger it (the
     * "if it isn't a mana ability" clause). The activating player is baked as the non-targeting
     * {@code targetId} so a player-directed effect (e.g. {@code DealDamageToPlayersEffect(2, TARGET_PLAYER)})
     * acts on "that player".
     */
    public void checkOpponentActivatesNonManaAbilityTriggers(GameData gameData, UUID activatingPlayerId, Permanent activatedPermanent) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY)) {
                CardEffect resolved = resolveTriggeringPermanentConditional(gameData, perm, ownerId, activatedPermanent, effect);
                if (resolved == null) continue;
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        activatingPlayerId,
                        perm.getId());
                // "That player" is the opponent who activated the ability — set by the event, not chosen.
                trigger.setNonTargeting(true);
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on opponent non-mana ability activation ({})",
                        gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
            }
        });
    }

    /**
     * Unwraps a {@link TriggeringPermanentConditionalEffect} against the permanent whose event fired.
     * Returns the wrapped effect when the predicate matches (evaluated from {@code watcher}'s point of
     * view), the effect unchanged when it isn't a conditional, or {@code null} to signal the trigger
     * should be skipped for this watcher. Shared by the ability-activation trigger collectors.
     */
    private CardEffect resolveTriggeringPermanentConditional(GameData gameData, Permanent watcher, UUID watcherOwnerId,
                                                             Permanent triggeringPermanent, CardEffect effect) {
        if (!(effect instanceof TriggeringPermanentConditionalEffect conditional)) {
            return effect;
        }
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourceControllerId(watcherOwnerId);
        if (!predicateEvaluationService.matchesPermanentPredicate(triggeringPermanent, conditional.predicate(), filterContext)) {
            return null;
        }
        return conditional.wrapped();
    }

    /**
     * "Whenever you activate an ability, if it isn't a mana ability, you may pay {N} to copy it"
     * triggers (Rings of Brighthearth). Called after the non-mana ability has been put on the stack
     * so it can be snapshotted. Fires on every permanent the activating player controls that has an
     * {@link EffectSlot#ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY} effect — except for
     * {@code equippedCreatureOnly} triggers (Illusionist's Bracers), which instead fire on any
     * permanent attached to the ability's source permanent, and copy the ability for free.
     *
     * @param abilityEntry the activated ability's stack entry (already on the stack)
     * @param ability      the activated ability that was activated (retained for retargeting the copy)
     */
    public void checkControllerActivatesNonManaAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                               StackEntry abilityEntry, ActivatedAbility ability) {
        if (abilityEntry == null) return;

        Integer pendingLoyaltyCopies = gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingLoyaltyCopies != null && pendingLoyaltyCopies > 0 && ability.getLoyaltyCost() != null) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<CardEffect> copyEffects = new ArrayList<>(pendingLoyaltyCopies);
            for (int i = 0; i < pendingLoyaltyCopies; i++) {
                copyEffects.add(new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId));
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    abilityEntry.getCard(),
                    activatingPlayerId,
                    "Copy " + abilityEntry.getCard().getName() + "'s loyalty ability",
                    copyEffects
            ));
            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.remove(activatingPlayerId);
        }

        gameData.forEachPermanent((ownerId, perm) -> {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY)) {
                if (!(effect instanceof CopyControllerActivatedAbilityTriggerEffect trigger)) continue;
                if (trigger.equippedCreatureOnly()) {
                    // "an ability of equipped creature" — the ability's source must be what this
                    // permanent is attached to, whoever activated it.
                    if (perm.getAttachedTo() == null
                            || !perm.getAttachedTo().equals(abilityEntry.getSourcePermanentId())) {
                        continue;
                    }
                } else if (!ownerId.equals(activatingPlayerId)) {
                    continue;
                }
                if (trigger.sourceFilter() != null && !predicateEvaluationService.matchesStackEntryPredicate(
                        abilityEntry, trigger.sourceFilter(), null)) {
                    continue;
                }
                if (trigger.loyaltyAbilityOnly() && ability.getLoyaltyCost() == null) {
                    continue;
                }

                StackEntry snapshot = new StackEntry(abilityEntry);
                // CR 707.10 — the copy is controlled by the controller of the effect that created it.
                UUID copyControllerId = trigger.equippedCreatureOnly() ? ownerId : activatingPlayerId;
                CardEffect copyEffect = new CopyControllerActivatedAbilityEffect(
                        snapshot, ability, copyControllerId);
                if (trigger.manaCost() != null) {
                    copyEffect = new MayPayManaEffect(
                            trigger.manaCost(),
                            copyEffect,
                            "Pay " + trigger.manaCost() + " to copy " + abilityEntry.getCard().getName() + "'s ability?");
                }

                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(copyEffect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on non-mana ability activation ({})",
                        gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
            }
        });
    }

    // ── Life-loss triggers ─────────────────────────────────────────────

    public void checkLifeLossTriggers(GameData gameData, UUID losingPlayerId, int lifeLostAmount) {
        if (lifeLostAmount <= 0) return;

        // Accumulate life lost this turn (damage funnels through here too — "damage causes loss of
        // life"). Read by Wound Reflection at end of turn.
        gameData.lifeLostThisTurn.merge(losingPlayerId, lifeLostAmount, Integer::sum);

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LifeLoss(losingPlayerId, lifeLostAmount);

        // Snapshot: handlers may modify the battlefield (e.g. Mindcrank mills → Undead Alchemist creates tokens)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LOSES_LIFE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (dispatch(match, EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        // Controller-loses-life triggers (e.g. Lich's Mastery)
        // Snapshot: handlers may modify the battlefield (exile permanents)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_LOSES_LIFE)) {
                    CardEffect toDispatch = effect;
                    if (effect instanceof OncePerTurnTriggerEffect once) {
                        if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                            continue;
                        }
                        toDispatch = once.wrapped();
                    }
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (registry.dispatch(match, EffectSlot.ON_CONTROLLER_LOSES_LIFE, toDispatch, ctx)) {
                        anyTriggered[0] = true;
                        if (effect instanceof OncePerTurnTriggerEffect) {
                            gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                        }
                    }
                }
            }
        });

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    // ── Life-gain triggers ──────────────────────────────────────────────

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        checkLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount, null, null);
    }

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount,
            Card sourceCard, StackEntryType sourceEntryType) {
        if (lifeGainedAmount <= 0) return;

        var ctx = new TriggerContext.LifeGain(gainingPlayerId, lifeGainedAmount, sourceCard, sourceEntryType);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(gainingPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    dispatch(match, EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);
                }
            }
        });

        collectLifeGainOpponentLifeLossTriggers(gameData, gainingPlayerId, lifeGainedAmount);
    }

    /**
     * Fires the turn-scoped "whenever you gain life this turn, each opponent loses that much life"
     * delayed triggers (Vizkopa Guildmage). One trigger per watcher whose controller is the player
     * who gained life; the amount rides on the entry's event value.
     */
    private void collectLifeGainOpponentLifeLossTriggers(GameData gameData, UUID gainingPlayerId,
                                                        int lifeGainedAmount) {
        for (LifeGainOpponentLifeLossWatcher watcher : List.copyOf(gameData.lifeGainOpponentLifeLossWatchers)) {
            if (!watcher.controllerId().equals(gainingPlayerId)) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT))),
                    (UUID) null,
                    (UUID) null);
            entry.setEventValue(lifeGainedAmount);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers (controller gained {} life), each opponent loses that much life",
                    gameData.id, watcher.sourceCard().getName(), lifeGainedAmount);
        }
    }

    // ── Creature-card-milled triggers ─────────────────────────────────

    public void checkCreatureCardMilledTriggers(GameData gameData, UUID milledPlayerId, Card milledCard) {
        var ctx = new TriggerContext.CreatureCardMilled(milledPlayerId, milledCard);

        // Snapshot battlefields: trigger handlers may add tokens to the battlefield
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(milledPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    dispatch(match, EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED, effect, ctx);
                }
            }
        });
    }

    // ── Noncombat-damage-to-opponent triggers ──────────────────────────

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId) {
        var ctx = new TriggerContext.NoncombatDamageToOpponent(damagedPlayerId);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(damagedPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    dispatch(match, EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);
                }
            }
        });
    }

    /**
     * Chandra's Phoenix: "Whenever an opponent is dealt damage by a red instant or sorcery spell you
     * control or by a red planeswalker you control, return this card from your graveyard to your hand."
     *
     * <p>{@code entry} is the stack entry that dealt the damage. It qualifies when it is a red instant
     * or sorcery spell, or an ability whose source card is a red planeswalker. The graveyard scanned is
     * the entry controller's ("you control"), and the damaged player must be one of their opponents.
     */
    public void checkRedSpellOrPlaneswalkerDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
                                                                    StackEntry entry) {
        if (entry == null) return;

        UUID controllerId = entry.getControllerId();
        if (controllerId == null || controllerId.equals(damagedPlayerId)) return;

        Card sourceCard = entry.getEffectiveDamageSourceCard();
        if (sourceCard == null || !sourceCard.getColors().contains(CardColor.RED)) return;

        boolean redSpell = entry.getEntryType() == StackEntryType.INSTANT_SPELL
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL;
        boolean redPlaneswalker = sourceCard.hasType(CardType.PLANESWALKER);
        if (!redSpell && !redPlaneswalker) return;

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects =
                    card.getEffects(EffectSlot.GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }

    // ── Queue-processing delegates ─────────────────────────────────────

    public void processNextDeathTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDeathTriggerTarget(gameData);
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackTriggerTarget(gameData);
    }

    public void processNextAttackCounterMoveFirstTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackCounterMoveFirstTarget(gameData);
    }

    public List<UUID> targetableCreaturesControlledBy(GameData gameData, UUID playerId,
                                                      Card sourceCard, UUID choosingPlayerId) {
        return triggeredAbilityQueueService.targetableCreaturesControlledBy(gameData, playerId, sourceCard, choosingPlayerId);
    }

    public void processNextEntersTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEntersTriggerTarget(gameData);
    }

    public void processNextSelfTriggeredAbilityTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSelfTriggeredAbilityTarget(gameData);
    }

    public void processNextTriggeredModalTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextTriggeredModalTrigger(gameData);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, ChooseOneEffect.ChooseOneOption chosen) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen);
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardSelfTrigger(gameData);
    }

    public void processNextDiscardControllerTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardControllerTriggerTarget(gameData);
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellTargetTrigger(gameData);
    }

    public void processNextETBSpellTargetTrigger(GameData gameData) {
        etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
    }

    public void processNextSpellGraveyardTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
    }

    public void processNextLifeGainTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextLifeGainTriggerTarget(gameData);
    }

    public void processNextDrawTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDrawTriggerTarget(gameData);
    }

    public void processNextEnteringPermanentAnyTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEnteringPermanentAnyTarget(gameData);
    }

    public void processNextSagaChapterTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterTarget(gameData);
    }

    public void processNextSagaChapterGraveyardTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterGraveyardTarget(gameData);
    }

    // ── Explore triggers ──────────────────────────────────────────────

    /**
     * Scans the exploring creature's controller's battlefield for permanents
     * with {@link EffectSlot#ON_ALLY_CREATURE_EXPLORES} effects and queues
     * them for target selection or directly onto the stack.
     */
    public void checkExploreTriggers(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_EXPLORES);
            if (effects == null || effects.isEmpty()) continue;

            boolean anyTargeting = effects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (anyTargeting) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.ExploreTriggerTarget(
                                perm.getCard(), controllerId, new ArrayList<>(effects), perm.getId()));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        null,
                        perm.getId()
                ));
            }
            log.info("Game {} - {} explore trigger queued", gameData.id, perm.getCard().getName());
        }
    }

    public void processNextExploreTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploreTriggerTarget(gameData);
    }

    // ── Exploit triggers ──────────────────────────────────────────────

    /**
     * Queues this permanent's {@link EffectSlot#ON_EXPLOIT} ability after it successfully
     * exploited a creature. Uses the source card's LKI even if the permanent left the battlefield
     * (e.g. it sacrificed itself).
     */
    public void checkExploitTriggers(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
        List<CardEffect> effects = sourceCard.getEffects(EffectSlot.ON_EXPLOIT);
        if (effects == null || effects.isEmpty()) return;

        boolean targetsStack = effects.stream().anyMatch(EffectResolution::targetsSpellOnStack);
        if (targetsStack) {
            StackEntryPredicate stackFilter = null;
            boolean includeAbilities = false;
            if (sourceCard.getTargetFilter() instanceof com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter sf) {
                stackFilter = sf.predicate();
                includeAbilities = predicateContainsHasTarget(sf.predicate());
            }
            gameData.queueInteraction(new PermanentChoiceContext.ExploitTriggerTarget(
                    sourceCard, controllerId, new ArrayList<>(effects), sourcePermanentId,
                    stackFilter, includeAbilities));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s exploit ability",
                    new ArrayList<>(effects),
                    null,
                    sourcePermanentId
            ));
        }
        gameLogService.append(gameData,
                GameLog.cardThen(sourceCard, " exploits a creature."));
        log.info("Game {} - {} exploit trigger queued", gameData.id, sourceCard.getName());
    }

    public void processNextExploitTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploitTriggerTarget(gameData);
    }

    public static boolean predicateContainsHasTarget(com.github.laxika.magicalvibes.model.filter.StackEntryPredicate predicate) {
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate) {
            return true;
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(TriggerCollectionService::predicateContainsHasTarget);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(TriggerCollectionService::predicateContainsHasTarget);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate not) {
            return predicateContainsHasTarget(not.predicate());
        }
        return false;
    }

    // ── Clash ──────────────────────────────────────────────────────────

    /**
     * Performs a clash for {@code clashingPlayerId} against their (single, 2-player) opponent
     * (MTG rule 701.29): both players reveal the top card of their library, the clashing player
     * wins if their revealed card's mana value is strictly greater than the opponent's, and then
     * {@link EffectSlot#ON_CONTROLLER_CLASHES} triggers on the clashing player's permanents.
     *
     * <p>Each player may put their revealed card on the bottom of their library; this engine leaves
     * the revealed cards on top (a legal choice), so no clash-source card yet mutates library order.
     * The "whenever you clash" triggers fire after the clash ends. Invoked from a clash-source card's
     * effect resolution (see {@code ClashEffect}) or a test.
     *
     * @return {@code true} if the clashing player won the clash (their revealed card had a strictly
     *         greater mana value), so callers can apply an "if you won" reward.
     */
    public boolean performClash(GameData gameData, UUID clashingPlayerId) {
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(clashingPlayerId))
                .findFirst().orElse(null);

        Card clashingCard = topCard(gameData, clashingPlayerId);
        Card opponentCard = opponentId != null ? topCard(gameData, opponentId) : null;

        GameLog.Builder clashLog = GameLog.builder()
                .text(gameData.playerIdToName.get(clashingPlayerId) + " clashes: reveals ");
        if (clashingCard != null) {
            clashLog.card(clashingCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text("; opponent reveals ");
        if (opponentCard != null) {
            clashLog.card(opponentCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text(".");
        gameLogService.append(gameData, clashLog.build());

        // 701.29c: win if your revealed card's mana value is higher than each other revealed card.
        boolean won = clashingCard != null
                && (opponentCard == null || clashingCard.getManaValue() > opponentCard.getManaValue());

        String outcome = won
                ? gameData.playerIdToName.get(clashingPlayerId) + " won the clash."
                : "No one won the clash.";
        gameLogService.append(gameData, GameLog.text(outcome));
        log.info("Game {} - {} clashes (won={})", gameData.id, clashingPlayerId, won);

        fireClashTriggers(gameData, clashingPlayerId, won);
        return won;
    }

    private Card topCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        return (deck == null || deck.isEmpty()) ? null : deck.getFirst();
    }

    private void fireClashTriggers(GameData gameData, UUID clashingPlayerId, boolean won) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(clashingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_CLASHES);
            if (effects == null || effects.isEmpty()) continue;

            // Resolve win-conditional clauses now: the clash has ended, so the winner is fixed.
            List<CardEffect> resolvedEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                if (effect instanceof ClashOutcomeConditionalEffect clashOutcome) {
                    if (clashOutcome.appliesOnWin() == won) resolvedEffects.add(clashOutcome.wrapped());
                } else {
                    resolvedEffects.add(effect);
                }
            }
            if (resolvedEffects.isEmpty()) continue;

            // Targeting clash triggers (Entangling Trap) route through the ClashTriggerTarget
            // interaction to pick an opponent's creature; non-targeting ones (Rebellion of the
            // Flamekin) go straight onto the stack as a triggered ability.
            boolean needsTarget = resolvedEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            if (needsTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.ClashTriggerTarget(
                        perm.getCard(), clashingPlayerId, resolvedEffects, perm.getId()));
                log.info("Game {} - {} clash trigger queued", gameData.id, perm.getCard().getName());
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        clashingPlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(resolvedEffects),
                        null,
                        perm.getId()));
                log.info("Game {} - {} clash trigger pushed to stack", gameData.id, perm.getCard().getName());
            }
        }

        triggeredAbilityQueueService.processNextClashTriggerTarget(gameData);
    }

    public void processNextClashTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextClashTriggerTarget(gameData);
    }

    // ── Death / leaves-battlefield triggers ───────────────────────────

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature) {
        collectDeathTrigger(gameData, dyingCard, controllerId, wasCreature, null);
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature, Permanent dyingPermanent) {
        List<CardEffect> deathEffects = dyingPermanent != null && dyingPermanent.isFaceDown()
                ? List.of() : dyingCard.getEffects(EffectSlot.ON_DEATH);

        // Include temporarily granted ON_DEATH effects (e.g. from Verdant Rebirth)
        List<CardEffect> temporaryDeathEffects = dyingPermanent != null
                ? dyingPermanent.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH) : List.of();

        // Include ON_DEATH abilities granted continuously by an attached Aura/Equipment
        // (Infernal Scarring). Read straight off the attachments, not through the layer system:
        // the dying permanent has already left the battlefield, but its Aura is still attached
        // (orphaned Auras only fall off in a later state-based-action pass).
        List<CardEffect> grantedDeathEffects = dyingPermanent != null
                ? grantedTriggeredAbilitySupport.grantedTriggeredEffectsFromAttachments(
                        gameData, dyingPermanent.getId(), EffectSlot.ON_DEATH)
                : List.of();

        if (deathEffects.isEmpty() && temporaryDeathEffects.isEmpty() && grantedDeathEffects.isEmpty()) return;

        var ctx = new TriggerContext.SelfDeath(dyingCard, controllerId, wasCreature, dyingPermanent);
        Permanent perm = dyingPermanent != null ? dyingPermanent : new Permanent(dyingCard);
        for (CardEffect effect : deathEffects) {
            // "When you sacrifice this" effects are collected from the sacrifice path instead.
            if (effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId);
            if (resolvedEffect == null) continue;
            var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
        for (CardEffect effect : temporaryDeathEffects) {
            if (effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId);
            if (resolvedEffect == null) continue;
            var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
        for (CardEffect effect : grantedDeathEffects) {
            if (effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId);
            if (resolvedEffect == null) continue;
            var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
    }

    public void checkAllyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Permanent dyingPermanent) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                dyingPermanent.getEffectivePower(), dyingPermanent.getEffectiveToughness());

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            boolean anyEffectFired = false;
            List<CardEffect> stackEffects = new ArrayList<>();

            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = unwrapCreatureDeathConditional(
                        effect, dyingCard, dyingPermanent, gameData, dyingCreatureControllerId);
                if (resolvedEffect == null) continue;

                if (resolvedEffect instanceof MayPayManaEffect || resolvedEffect instanceof MayEffect) {
                    var match = new TriggerMatchContext(gameData, perm, dyingCreatureControllerId, resolvedEffect);
                    dispatch(match, EffectSlot.ON_ALLY_CREATURE_DIES, resolvedEffect, ctx);
                    anyEffectFired = true;
                } else if (resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER) || resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                    // Targeted "another creature you control dies" trigger (e.g. Diregraf Captain):
                    // route through the death target pipeline so the controller picks a target as the
                    // ability is put on the stack (CR 603.3d). The source card here is the watching
                    // permanent, so its own target filter (e.g. opponent-only) is honoured.
                    // The dying creature's last-known power rides along as the event value, so an
                    // amount declared as EventValue resolves to it (Death's Presence — "put X +1/+1
                    // counters on target creature you control, where X is the power of the creature
                    // that died").
                    gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                            perm.getCard(), dyingCreatureControllerId, new ArrayList<>(List.of(resolvedEffect)),
                            Math.max(0, dyingPermanent.getEffectivePower())
                    ));
                    anyEffectFired = true;
                } else {
                    // Enduring Renewal / similar: bind the dying card id onto effects that need it
                    // before the batched stack entry is created.
                    if (resolvedEffect instanceof DyingCreatureCardAwareEffect aware
                            && dyingCard != null) {
                        resolvedEffect = aware.boundToDyingCard(dyingCard.getId());
                    }
                    if (resolvedEffect instanceof DyingCreatureCounterAwareEffect aware) {
                        resolvedEffect = aware.boundToDyingCreatureCounterCount(
                                countCountersOnPermanent(dyingPermanent));
                    }
                    stackEffects.add(resolvedEffect);
                }
            }

            if (!stackEffects.isEmpty()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        dyingCreatureControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(stackEffects),
                        null,
                        perm.getId()
                ));
                anyEffectFired = true;
            }

            if (anyEffectFired) {
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (ally creature died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    /**
     * "Whenever a creature or planeswalker you control dies" ({@link EffectSlot#ON_ALLY_CREATURE_OR_PLANESWALKER_DIES}).
     * Called once per dying permanent that was a creature and/or a planeswalker, so a permanent
     * that is both only triggers once.
     */
    public void checkAllyCreatureOrPlaneswalkerDeathTriggers(GameData gameData, UUID dyingControllerId,
            Permanent dyingPermanent) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingControllerId);
        if (battlefield == null) return;

        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingControllerId,
                dyingPermanent.getEffectivePower(), dyingPermanent.getEffectiveToughness());

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_OR_PLANESWALKER_DIES);
            if (effects == null || effects.isEmpty()) continue;

            boolean anyEffectFired = false;
            List<CardEffect> stackEffects = new ArrayList<>();

            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = unwrapCreatureDeathConditional(
                        effect, dyingCard, dyingPermanent, gameData, dyingControllerId);
                if (resolvedEffect == null) continue;

                if (resolvedEffect instanceof MayPayManaEffect || resolvedEffect instanceof MayEffect) {
                    var match = new TriggerMatchContext(gameData, perm, dyingControllerId, resolvedEffect);
                    dispatch(match, EffectSlot.ON_ALLY_CREATURE_OR_PLANESWALKER_DIES, resolvedEffect, ctx);
                    anyEffectFired = true;
                } else {
                    stackEffects.add(resolvedEffect);
                }
            }

            if (!stackEffects.isEmpty()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        dyingControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(stackEffects),
                        null,
                        perm.getId()
                ));
                anyEffectFired = true;
            }

            if (anyEffectFired) {
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (ally creature or planeswalker died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    public void checkEquippedCreatureDeathTriggers(GameData gameData, UUID dyingCreatureId, UUID dyingCreatureControllerId, Card dyingCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.EquippedCreatureDeath(dyingCreatureId, dyingCreatureControllerId, dyingCard);

        for (Permanent perm : battlefield) {
            if (!dyingCreatureId.equals(perm.getAttachedTo())) continue;
            if (!perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = unwrapTriggeringCardConditional(effect, dyingCard, gameData, dyingCreatureControllerId);
                if (resolvedEffect == null) continue;
                var match = new TriggerMatchContext(gameData, perm, dyingCreatureControllerId, resolvedEffect);
                dispatch(match, EffectSlot.ON_EQUIPPED_CREATURE_DIES, resolvedEffect, ctx);
            }
        }
    }

    public void checkEnchantedPermanentDeathTriggers(GameData gameData, UUID dyingPermanentId,
                                                      UUID dyingPermanentControllerId, UUID dyingCreatureCardId,
                                                      int dyingCreaturePower, int dyingCreatureToughness) {
        var ctx = new TriggerContext.EnchantedPermanentDeath(dyingPermanentId, dyingPermanentControllerId,
                dyingCreatureCardId, dyingCreaturePower, dyingCreatureToughness);

        gameData.forEachPermanent((playerId, perm) -> {
            if (!dyingPermanentId.equals(perm.getAttachedTo())) return;
            if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, effect, ctx);
            }
        });
    }

    public void checkEnchantedPermanentLTBTriggers(GameData gameData, Permanent leavingPermanent, UUID leavingControllerId) {
        var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPermanent, leavingControllerId);

        gameData.forEachPermanent((playerId, perm) -> {
            if (!leavingPermanent.getId().equals(perm.getAttachedTo())) return;
            if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, effect, ctx);
            }
        });
    }

    public void checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId, UUID artifactControllerId) {
        var ctx = new TriggerContext.ArtifactGraveyard(graveyardOwnerId, artifactControllerId);

        gameData.forEachPermanent((playerId, perm) -> {
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);

            if (!playerId.equals(graveyardOwnerId)) {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            }
        });
    }

    public void checkAnyLandPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId, UUID landControllerId) {
        var ctx = new TriggerContext.AnyLandGraveyard(graveyardOwnerId, landControllerId);

        gameData.forEachPermanent((playerId, perm) ->
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx));
    }

    public void checkAnyEnchantmentPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId, UUID enchantmentControllerId) {
        var ctx = new TriggerContext.EnchantmentGraveyard(graveyardOwnerId, enchantmentControllerId);

        gameData.forEachPermanent((playerId, perm) -> {
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            if (playerId.equals(enchantmentControllerId)) {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            }
        });
    }

    /**
     * Fires ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT triggers (Sacred Ground). Only fires when a
     * spell or ability an opponent of the graveyard owner controls caused the land to be put into the
     * graveyard, and only on permanents the graveyard owner controls.
     */
    public void checkLandPutIntoGraveyardByOpponentTriggers(GameData gameData, Card landCard,
                                                            UUID graveyardOwnerId, UUID causeControllerId) {
        if (causeControllerId == null || causeControllerId.equals(graveyardOwnerId)) return;

        var ctx = new TriggerContext.LandPutIntoGraveyard(landCard, graveyardOwnerId, causeControllerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT, ctx);
        }
    }

    /** Fires "whenever you win a coin flip" triggers for the winning player's battlefield. */
    public void checkControllerWinsCoinFlipTriggers(GameData gameData, UUID winningPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(winningPlayerId);
        if (battlefield == null) return;

        TriggerContext ctx = new TriggerContext.CoinFlipWon(winningPlayerId);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_WINS_COIN_FLIP)) {
                var match = new TriggerMatchContext(gameData, perm, winningPlayerId, effect);
                registry.dispatch(match, EffectSlot.ON_CONTROLLER_WINS_COIN_FLIP, effect, ctx);
            }
        }
    }

    /**
     * Fires Karmic Justice triggers for a noncreature permanent actually destroyed while resolving
     * an opponent's spell or ability. The destroyed permanent is also checked as a source because
     * its triggered ability still triggers when it is destroyed by that spell or ability.
     */
    public void checkNoncreaturePermanentDestroyedByOpponentTriggers(GameData gameData,
                                                                      Permanent destroyedPermanent,
                                                                      UUID destroyedControllerId,
                                                                      UUID causeControllerId) {
        if (causeControllerId == null || causeControllerId.equals(destroyedControllerId)) return;

        var ctx = new TriggerContext.NoncreaturePermanentDestroyed(
                destroyedPermanent.getCard(), destroyedControllerId, causeControllerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(destroyedControllerId);
        if (battlefield != null) {
            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchSlot(gameData, perm, destroyedControllerId,
                        EffectSlot.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT, ctx);
            }
        }
        if (!destroyedPermanent.getCard().getEffects(
                EffectSlot.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT).isEmpty()) {
            dispatchSlot(gameData, destroyedPermanent, destroyedControllerId,
                    EffectSlot.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT, ctx);
        }
    }

    /**
     * Fires ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE triggers (Countryside Crusher) whenever a
     * land card is put into its owner's graveyard from any zone. Fires on every permanent the graveyard
     * owner controls that has this slot.
     */
    public void checkLandPutIntoGraveyardFromAnywhereTriggers(GameData gameData, UUID graveyardOwnerId, Card landCard) {
        var ctx = new TriggerContext.LandPutIntoGraveyard(landCard, graveyardOwnerId, null);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
        }
    }

    /** Fires library-origin land-card triggers after a land has actually entered the graveyard. */
    public void checkLandCardMilledTriggers(GameData gameData, UUID graveyardOwnerId, Card landCard) {
        var ctx = new TriggerContext.LandCardMilled(landCard, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_ALLY_LAND_CARD_MILLED, ctx);
        }
    }

    /**
     * Fires controller-graveyard triggers whenever a non-token card enters the controller's
     * graveyard from any zone.
     */
    public void checkCardPutIntoGraveyardFromAnywhereTriggers(GameData gameData, UUID graveyardOwnerId,
                                                               Card card) {
        var ctx = new TriggerContext.CardPutIntoGraveyard(card, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId,
                    EffectSlot.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
        }
    }

    /**
     * Fires ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE triggers (Soulcipher Board) whenever
     * a creature card is put into its owner's graveyard from any zone. Uses printed card types (not
     * battlefield creature-ness); callers must already exclude tokens. Fires on every permanent the
     * graveyard owner controls that has this slot, and on every permanent an opponent of the
     * graveyard owner controls that has ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE
     * (Profane Memento).
     */
    public void checkCreatureCardPutIntoGraveyardFromAnywhereTriggers(GameData gameData, UUID graveyardOwnerId,
            Card creatureCard) {
        var ctx = new TriggerContext.CreatureCardPutIntoGraveyard(creatureCard, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield != null) {
            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchSlot(gameData, perm, graveyardOwnerId,
                        EffectSlot.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
            }
        }

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(graveyardOwnerId)) return;
            dispatchSlot(gameData, perm, playerId,
                    EffectSlot.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);
        });
    }

    /**
     * Fires opponent-graveyard triggers whenever a non-token card enters an opponent's graveyard
     * from any zone. The graveyard owner is preserved as the trigger's target context.
     */
    public void checkCardPutIntoOpponentGraveyardFromAnywhereTriggers(GameData gameData,
            UUID graveyardOwnerId, Card card) {
        var ctx = new TriggerContext.CardPutIntoGraveyard(card, graveyardOwnerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(graveyardOwnerId)) return;
            dispatchSlot(gameData, perm, playerId,
                    EffectSlot.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);
        });
    }

    public void checkPermanentCardPutIntoGraveyardFromAnywhereTriggers(GameData gameData,
            UUID graveyardOwnerId, Card permanentCard) {
        var ctx = new TriggerContext.PermanentCardPutIntoGraveyard(permanentCard, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId,
                    EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
        }
    }

    /**
     * Fires ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE triggers (Compost). The card has
     * already entered the graveyard; fires on permanents controlled by an opponent of the graveyard
     * owner when the card is black.
     */
    public void checkBlackCardPutIntoOpponentGraveyardFromAnywhereTriggers(GameData gameData, UUID graveyardOwnerId, Card card) {
        if (card.getColors() == null || !card.getColors().contains(CardColor.BLACK)) return;

        var ctx = new TriggerContext.BlackCardOpponentGraveyard(graveyardOwnerId, card);

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(graveyardOwnerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);
        });
    }

    /**
     * Fires ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers (Prince of Thralls)
     * whenever a permanent of any type is put into a graveyard from the battlefield. Fires on
     * permanents controlled by an opponent of the dying permanent's controller.
     */
    public void checkOpponentPermanentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard,
                                                               UUID dyingControllerId, UUID graveyardOwnerId) {
        var ctx = new TriggerContext.OpponentPermanentGraveyard(dyingCard, dyingControllerId, graveyardOwnerId);

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(dyingControllerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        });
    }

    /**
     * Fires ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers
     * (Kothophed, Soul Hoarder). Ownership-based: only watchers controlled by a player other than the
     * dying permanent's owner see it.
     */
     public void checkOtherPlayerOwnedPermanentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard, UUID ownerId) {
        var ctx = new TriggerContext.OtherPlayerOwnedPermanentGraveyard(dyingCard, ownerId);

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(ownerId)) return;
             dispatchSlot(gameData, perm, playerId,
                     EffectSlot.ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        });
    }

    /**
     * Fires ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers (Yomiji, Who Bars the Way)
     * whenever a permanent of any type is put into a graveyard from the battlefield. Fires on every
     * permanent still on the battlefield, so the dead permanent never sees its own death. Supports
     * {@code TriggeringCardConditionalEffect} gating on the dead permanent's card.
     */
    public void checkAnyPermanentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard,
                                                          UUID dyingControllerId, UUID graveyardOwnerId) {
        var ctx = new TriggerContext.AnyPermanentGraveyard(dyingCard, dyingControllerId, graveyardOwnerId);

        gameData.forEachPermanent((playerId, perm) -> {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            if (perm.getCard().getId().equals(dyingCard.getId())) return;
            for (CardEffect effect : perm.getCard()
                    .getEffects(EffectSlot.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, dyingCard, gameData, playerId);
                if (resolved == null) continue;
                var match = new TriggerMatchContext(gameData, perm, playerId, resolved);
                dispatch(match, EffectSlot.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                        resolved, ctx);
            }

            if (playerId.equals(graveyardOwnerId) && !dyingCard.isToken()) {
                dispatchSlot(gameData, perm, playerId,
                        EffectSlot.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            }

            if (playerId.equals(graveyardOwnerId)) return;
             dispatchSlot(gameData, perm, playerId,
                     EffectSlot.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        });
    }

    public void checkAnyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Permanent dyingPermanent) {
        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                dyingPermanent.getEffectivePower(), dyingPermanent.getEffectiveToughness());

        gameData.forEachPermanent((playerId, perm) -> {
            dispatchAnyCreatureDeathTriggersForWatcher(gameData, playerId, perm, dyingPermanent, dyingCreatureControllerId, ctx);
        });

        // Last-known watchers dying in the same simultaneous event (CR 603.6c / 603.10). Skip self
        // ("other creatures") and skip watchers still on the battlefield (already handled above).
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            UUID watcherId = entry.getKey();
            if (watcherId.equals(dyingPermanent.getId())) continue;
            if (gameQueryService.findPermanentById(gameData, watcherId) != null) continue;
            Permanent watcher = entry.getValue();
            UUID controllerId = gameData.simultaneousDyingControllers.get(watcherId);
            if (controllerId == null) continue;
            dispatchAnyCreatureDeathTriggersForWatcher(
                    gameData, controllerId, watcher, dyingPermanent, dyingCreatureControllerId, ctx);
        }

        collectTemporaryGlobalTriggers(gameData, EffectSlot.ON_ANY_CREATURE_DIES,
                dyingCreatureControllerId, Math.max(0, ctx.dyingCreatureToughness()));

        collectEmblemCreatureDeathTriggers(gameData, dyingCard);
        collectCreatureDeathTriggerWatchers(gameData);
    }

    private void collectCreatureDeathTriggerWatchers(GameData gameData) {
        for (CreatureDeathTriggerWatcher watcher : List.copyOf(gameData.creatureDeathTriggerWatchers)) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because a creature died",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * Emblem creature-death triggers — Liliana, Defiant Necromancer's "Whenever a creature dies,
     * return it to the battlefield under your control at the beginning of the next end step."
     * Emblems live outside the battlefield, so the permanent sweep above never sees them.
     */
    private void collectEmblemCreatureDeathTriggers(GameData gameData, Card dyingCard) {
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof RegisterDelayedReturnDyingCreatureUnderControlEffect delayedReturn)) {
                    continue;
                }
                Card source = emblem.sourceCard();
                String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source != null ? source : dyingCard,
                        emblem.controllerId(),
                        desc,
                        new ArrayList<>(List.of(delayedReturn))
                );
                entry.setTriggeringCardId(dyingCard.getId());
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.text(desc + " triggers (a creature died)."));
                log.info("Game {} - {} schedules a delayed return of {}", gameData.id, desc, dyingCard.getName());
            }
        }
    }

    private void dispatchAnyCreatureDeathTriggersForWatcher(GameData gameData, UUID playerId, Permanent perm,
            Permanent dyingPermanent, UUID dyingCreatureControllerId, TriggerContext.CreatureDeath ctx) {
        List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_DIES);
        if (effects == null || effects.isEmpty()) return;

        Card dyingCard = dyingPermanent.getCard();
        for (CardEffect effect : effects) {
            CardEffect toResolve = effect;
            if (effect instanceof OncePerTurnTriggerEffect once) {
                if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                    continue;
                }
                toResolve = once.wrapped();
            }
            // Death conditionals may reference the dying creature's on-battlefield state (e.g.
            // Blowfly Infestation's "if it had a -1/-1 counter on it") — evaluate against the
            // dying permanent, not just its card.
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(
                    toResolve, dyingCard, dyingPermanent, gameData, dyingCreatureControllerId);
            if (resolvedEffect == null) continue;
            var match = new TriggerMatchContext(gameData, perm, playerId, resolvedEffect);
            if (dispatch(match, EffectSlot.ON_ANY_CREATURE_DIES, resolvedEffect, ctx)
                    && effect instanceof OncePerTurnTriggerEffect) {
                gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
            }
        }
    }

    public void checkAllyNontokenCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Card dyingCard) {
        if (dyingCard.isToken()) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                dyingCard.getPower() != null ? dyingCard.getPower() : 0,
                dyingCard.getToughness() != null ? dyingCard.getToughness() : 0);

        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, dyingCreatureControllerId, EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, ctx);
        }
    }

    public void checkAnyNontokenCreatureDeathTriggers(GameData gameData, Card dyingCard) {
        if (dyingCard.isToken()) return;

        var ctx = new TriggerContext.CreatureDeath(dyingCard, null,
                dyingCard.getPower() != null ? dyingCard.getPower() : 0,
                dyingCard.getToughness() != null ? dyingCard.getToughness() : 0);

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                CardEffect toDispatch = effect;
                if (effect instanceof OncePerTurnTriggerEffect once) {
                    if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                        continue;
                    }
                    toDispatch = once.wrapped();
                }
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                if (dispatch(match, EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES, toDispatch, ctx)
                        && effect instanceof OncePerTurnTriggerEffect) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
            }
        });
    }

    public void checkOpponentCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Permanent dyingPermanent) {
        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                dyingPermanent.getEffectivePower(), dyingPermanent.getEffectiveToughness());

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(dyingCreatureControllerId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            List<CardEffect> effects = new ArrayList<>();
            List<CardEffect> ownEffects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES);
            if (ownEffects != null) effects.addAll(ownEffects);
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_OPPONENT_CREATURE_DIES));
            if (effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                // Death conditionals may reference the dying creature's on-battlefield state (e.g.
                // Necroskitter's "with a -1/-1 counter on it") — evaluate against the dying permanent.
                CardEffect resolvedEffect = unwrapCreatureDeathConditional(
                        effect, dyingCard, dyingPermanent, gameData, dyingCreatureControllerId);
                if (resolvedEffect == null) continue;
                var match = new TriggerMatchContext(gameData, perm, playerId, resolvedEffect);
                dispatch(match, EffectSlot.ON_OPPONENT_CREATURE_DIES, resolvedEffect, ctx);
            }
        });
    }

    public void checkSelfLeavesTriggered(GameData gameData, Permanent target, UUID controllerId) {
        List<CardEffect> effects = target.getCard().getEffects(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD);
        if (effects == null || effects.isEmpty()) return;

        var ctx = new TriggerContext.SelfLeaves(controllerId);

        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, target, controllerId, effect);
            dispatch(match, EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, effect, ctx);
        }
    }

    /**
     * Fires delayed "when that creature leaves the battlefield this turn, sacrifice this creature"
     * triggers (Kjeldoran Elite Guard). Drains matching {@link DelayedSacrificeSourceWhenTargetLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} for each. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeSourceWhenTargetLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeSourceWhenTargetLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeSourceWhenTargetLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeSourceWhenTargetLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.sourcePermanentId());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (watched {} left); sacrifice source {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.sourcePermanentId());
        }
    }

    /**
     * Fires delayed "when this creature leaves the battlefield this turn, sacrifice that creature"
     * triggers (Phantasmal Mount). Drains matching {@link DelayedSacrificeTargetWhenSourceLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} against the pumped target for each. Called
     * from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.targetPermanentId());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (source {} left); sacrifice target {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.targetPermanentId());
        }
    }

    /**
     * "Whenever another creature leaves the battlefield" triggers (e.g. Extractor Demon). Called from
     * every leave-the-battlefield path in {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}
     * (graveyard, hand, exile, library) after the permanent has been removed. Global watcher: fires on
     * every permanent with {@link EffectSlot#ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD}, on any battlefield,
     * except the leaving creature itself ("another"). No-op unless the leaving permanent was a creature
     * (last-known information, captured before removal). Queues a non-targeting triggered ability whose
     * {@code sourcePermanentId} is the watching permanent; any player targeting for a wrapped
     * {@code MayEffect} happens at resolution.
     */
    public void checkAnotherCreatureLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent, boolean wasCreature) {
        if (!wasCreature) return;
        UUID leavingId = leavingPermanent.getId();

        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.getId().equals(leavingId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another creature leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        });
    }

    /**
     * "Whenever another creature you control leaves the battlefield" triggers (e.g. Luminous Phantom).
     * Controller-scoped sibling of {@link #checkAnotherCreatureLeavesBattlefieldTriggers}.
     */
    public void checkAllyCreatureLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent,
                                                           boolean wasCreature, UUID controllerId) {
        if (!wasCreature || controllerId == null) return;
        UUID leavingId = leavingPermanent.getId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.getId().equals(leavingId)) continue;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another creature you control leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        }
    }

    /**
     * "Whenever another artifact you control leaves the battlefield" triggers (e.g. Sludge Strider).
     * Called from every leave-the-battlefield path in {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}
     * (graveyard, hand, exile, library) after the permanent has been removed. Controller-scoped
     * watcher: fires only on permanents on the leaving artifact's controller's battlefield with
     * {@link EffectSlot#ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD}, except the leaving artifact itself
     * ("another"). No-op unless the leaving permanent was an artifact (last-known information, read
     * off the permanent object which is unaffected by removal). Queues a non-targeting triggered
     * ability whose {@code sourcePermanentId} is the watching permanent; any player targeting for a
     * wrapped {@code MayPayManaEffect} happens at resolution.
     */
    public void checkAnotherArtifactLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent, UUID controllerId) {
        if (controllerId == null) return;
        if (!gameQueryService.isArtifact(leavingPermanent)) return;
        UUID leavingId = leavingPermanent.getId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.getId().equals(leavingId)) continue;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another artifact leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        }
    }

    public void checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingCard, controllerId);

        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, controllerId, EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        }
    }

    public void checkControllerCardsLeaveGraveyardTriggers(GameData gameData, UUID graveyardOwnerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.ControllerCardsLeaveGraveyard(graveyardOwnerId);

        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, ctx);
        }
    }

    public void checkControllerCreatureCardsLeaveGraveyardTriggers(GameData gameData, UUID graveyardOwnerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.ControllerCardsLeaveGraveyard(graveyardOwnerId);
        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD, ctx);
        }
    }

    /**
     * Fires {@link EffectSlot#GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD} for every card
     * sitting in an opponent's graveyard when a creature card leaves {@code graveyardOwnerId}'s
     * graveyard. The trigger lives on a card in a graveyard, so the ability's controller is that
     * card's owner and there is no source permanent.
     */
    public void checkCreatureCardLeavesOpponentGraveyardTriggers(GameData gameData, UUID graveyardOwnerId,
                                                                 Card leavingCard) {
        if (leavingCard == null || !leavingCard.hasType(CardType.CREATURE)) return;

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(graveyardOwnerId)) continue;

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;

            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = card.getEffects(EffectSlot.GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            playerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} triggers ({} left an opponent's graveyard)",
                            gameData.id, card.getName(), leavingCard.getName());
                }
            }
        }
    }

    public void triggerDelayedPoisonOnDeath(GameData gameData, UUID dyingCreatureCardId, UUID controllerId) {
        Integer poisonAmount = gameData.creatureGivingControllerPoisonOnDeathThisTurn.remove(dyingCreatureCardId);
        if (poisonAmount == null || poisonAmount <= 0) {
            return;
        }

        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, controllerId)) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
        gameData.playerPoisonCounters.put(controllerId, currentPoison + poisonAmount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gets " + poisonAmount + " poison counter"
                + (poisonAmount > 1 ? "s" : "") + " (delayed trigger: creature died this turn).";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {} poison counter(s) (delayed trigger: creature died this turn)",
                gameData.id, playerName, poisonAmount);
    }

    /**
     * Delayed "return that card when it dies this turn" (Graceful Reprieve): if the dying creature's
     * card was registered, push a triggered ability that returns it from its owner's graveyard to the
     * battlefield under its owner's control. Fires at most once per registration.
     */
    public void triggerDelayedReturnOnDeath(GameData gameData, UUID dyingCreatureCardId, Card graveyardCard, UUID ownerId) {
        Boolean enterTapped = gameData.creaturesReturnedToBattlefieldOnDeathThisTurn.remove(dyingCreatureCardId);
        if (enterTapped == null) {
            return;
        }

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                graveyardCard,
                ownerId,
                "Return " + graveyardCard.getName() + " to the battlefield",
                new ArrayList<>(List.of(new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(enterTapped)))
        ));

        gameLogService.append(gameData, GameLog.cardThen(graveyardCard, " will return to the battlefield (it died this turn)."));
        log.info("Game {} - Delayed return trigger: {} will return to the battlefield", gameData.id, graveyardCard.getName());
    }

    /**
     * Delayed "when that creature dies this turn, ..." (Skeletonize, Initiate of Blood): if the dying
     * creature's card was registered, push one triggered ability per registration that resolves the
     * recorded effect under the recorded controller's control, carrying the registering permanent so
     * self-referential effects still find it. Fires at most once per registration.
     */
    public void triggerDelayedEffectOnDeath(GameData gameData, UUID dyingCreatureCardId,
                                            UUID dyingCreatureControllerId, int dyingCreaturePower) {
        List<DelayedEffectOnDeath> registrations = gameData.creatureTriggeringEffectOnDeathThisTurn.remove(dyingCreatureCardId);
        if (registrations == null) {
            return;
        }

        for (DelayedEffectOnDeath registration : registrations) {
            StackEntry delayedEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    registration.sourceCard(),
                    registration.controllerId(),
                    registration.sourceCard().getName() + " triggers (a creature dealt damage this way died)",
                    new ArrayList<>(List.of(registration.effect())),
                    dyingCreatureControllerId,
                    registration.sourcePermanentId());
            delayedEntry.setEventValue(Math.max(0, dyingCreaturePower));
            gameData.stack.add(delayedEntry);

            log.info("Game {} - Delayed death trigger: {} triggers (a creature it damaged died this turn)",
                    gameData.id, registration.sourceCard().getName());
        }
    }

    // ── Enter-the-battlefield triggers ─────────────────────────────────

    /**
     * "Whenever a creature enters under your control" (ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).
     * The trigger count is carried through the enter-trigger context so stack entries and
     * deferred target choices are duplicated consistently.
     */
    /** Fires "whenever this creature or another creature you control is turned face up" triggers. */
    public void checkSelfOrAllyCreatureTurnsFaceUpTriggers(GameData gameData, UUID controllerId,
                                                            Permanent turnedPermanent) {
        if (!gameQueryService.isCreature(gameData, turnedPermanent)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        TriggerContext.PermanentTurnsFaceUp ctx = new TriggerContext.PermanentTurnsFaceUp(turnedPermanent, controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isFaceDown() || perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                registry.dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect),
                        EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP, effect, ctx);
            }
        }
    }

    public void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature, int extraWizardTriggers) {
        if (enteringCreature.getToughness() == null) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCreature, controllerId, null, 1 + extraWizardTriggers, null);

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            // "Whenever this creature or another creature you control enters" is the same scan
            // minus the self-exclusion, so it is checked before the source is skipped.
            List<CardEffect> selfOrAllyEffects = perm.getCard().getEffects(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD);
            if (selfOrAllyEffects != null) {
                for (CardEffect effect : selfOrAllyEffects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, controllerId);
                    if (resolved == null) continue;
                    resolved = resolveTriggeringPermanentConditional(
                            gameData, perm, controllerId, enteringPermanent, resolved);
                    if (resolved == null) continue;
                    resolved = unwrapEnterCreatureConditional(gameData, enteringCreature, perm, resolved);
                    if (resolved == null) continue;
                    boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                    resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                    if (resolved == null) continue;
                    if (!passesEnterInterveningIf(gameData, perm, controllerId, resolved)) continue;
                    if (dispatchEnter(gameData, perm, controllerId,
                            EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx)
                            && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                }
            }

            if (perm.getCard() == enteringCreature) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
            if (effects != null) {
                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, controllerId);
                    if (resolved == null) continue;
                    resolved = resolveTriggeringPermanentConditional(
                            gameData, perm, controllerId, enteringPermanent, resolved);
                    if (resolved == null) continue;
                    resolved = unwrapEnterCreatureConditional(gameData, enteringCreature, perm, resolved);
                    if (resolved == null) continue;
                    boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                    resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                    if (resolved == null) continue;
                    if (!passesEnterInterveningIf(gameData, perm, controllerId, resolved)) continue;
                    if (dispatchEnter(gameData, perm, controllerId,
                            EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx)
                            && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                }
            }

            if (enteringPermanent != null && !perm.isLosesAllAbilitiesUntilEndOfTurn()
                    && gameQueryService.hasKeyword(gameData, perm, Keyword.EVOLVE)) {
                collectEvolveTrigger(gameData, controllerId, perm, enteringPermanent,
                        1 + extraWizardTriggers);
            }
        }

        // Graveyard-resident creature-enters triggers (GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
        // e.g. Unconventional Tactics). A graveyard card is not a permanent, so it is excluded from Naban
        // doubling — hence added after the extra-trigger duplication above.
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = card.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, controllerId);
                    if (resolved == null) continue;

                    if (resolved instanceof MayEffect may) {
                        gameData.queueMayAbility(card, controllerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                controllerId,
                                card.getName() + "'s ability",
                                new ArrayList<>(List.of(resolved))
                        ));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} graveyard creature-enters trigger queued", gameData.id, card.getName());
                }
            }
        }
    }

    /**
     * "Whenever another creature enters" (ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD). Naban doubling
     * is applied per effect (only for permanents the entering creature's controller controls); the
     * default fallback skips targeting effects.
     */
    public void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        checkAnyCreatureEntersTriggers(gameData, enteringCreatureControllerId, enteringCreature,
                gameQueryService.countETBExtraTriggers(gameData, enteringCreatureControllerId, enteringCreature));
    }

    public void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                               Card enteringCreature, int extraWizardTriggers) {
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;
            if (perm.getCard() == enteringCreature) return;

            int extraTriggers = gameQueryService.countETBExtraTriggers(
                    gameData, playerId, enteringCreatureControllerId, enteringCreature);
            var ctx = new TriggerContext.PermanentEnters(
                    enteringCreature, enteringCreatureControllerId, null, 1 + extraTriggers, null);

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, playerId);
                if (resolved == null) continue;
                resolved = unwrapEnterCreatureConditional(gameData, enteringCreature, perm, resolved);
                if (resolved == null) continue;
                dispatchEnter(gameData, perm, playerId, EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx);
            }
        });
    }

    /**
     * Fires turn-scoped delayed triggers registered by Beck. Each registration is a separate
     * optional draw trigger, including for tokens and creatures controlled by the other player.
     */
    public void checkCreatureEntersThisTurnTriggers(GameData gameData, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.creatureEntersDrawSourcesThisTurn.get(controllerId);
            if (sources == null) continue;
            for (Card sourceCard : new ArrayList<>(sources)) {
                gameData.queueMayAbility(sourceCard, controllerId,
                        new MayEffect(new DrawCardEffect(), "Draw a card?"));
                log.info("Game {} - {} triggers for creature entering (may draw)",
                        gameData.id, sourceCard.getName());
            }
        }
    }

    /**
     * "Whenever a creature enchanted player controls enters" (ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD).
     * Scans every battlefield for player-enchanting Curse auras attached to the entering creature's controller
     * and queues one triggered ability each, controlled by the Aura's controller ("you"). The enchanted player
     * is baked as the (non-targeting) {@code targetId} so a {@code LoseLifeEffect(TARGET_PLAYER)} lands on them
     * while an accompanying {@code GainLifeEffect} feeds the controller. Used by Trespasser's Curse.
     */
    public void checkEnchantedPlayerCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachPermanent((auraControllerId, perm) -> {
            if (!perm.isAttached() || !enteringCreatureControllerId.equals(perm.getAttachedTo())) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                    gameData, auraControllerId, enteringCreatureControllerId, enteringCreature);
            for (int i = 0; i < triggerCount; i++) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        enteringCreatureControllerId,
                        perm.getId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} enchanted-player-creature-enters trigger queued", gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * "Whenever a player puts a permanent onto the battlefield" (ON_ANY_PERMANENT_ENTERS_BATTLEFIELD).
     * Fires for every entering permanent regardless of type or controller; wrap the effect in a
     * {@link TriggeringCardConditionalEffect} to restrict which permanents trigger it. The entering
     * permanent's controller is baked in as the non-targeting {@code targetId} so player-directed
     * effects act on "that player". The entering permanent's id is stamped on
     * {@code triggeringPermanentId} (and its card id on {@code triggeringCardId}) so effects can act
     * on "that permanent" / its name (Eye of Singularity). Used by Nature's Wrath.
     */
    public void checkAnyPermanentEntersTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        UUID enteringPermanentId = null;
        List<Permanent> enteringBattlefield = gameData.playerBattlefields.get(enteringControllerId);
        if (enteringBattlefield != null) {
            for (Permanent p : enteringBattlefield) {
                if (p.getCard() == enteringCard) {
                    enteringPermanentId = p.getId();
                    break;
                }
            }
        }
        final UUID resolvedEnteringPermanentId = enteringPermanentId;
        var ctx = new TriggerContext.PermanentEnters(
                enteringCard, enteringControllerId, enteringControllerId, 1, resolvedEnteringPermanentId);

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            // Each effect in this slot is its own triggered ability (Nature's Wrath has two), so a
            // card whose entry matches both conditions is dispatched separately.
            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, playerId);
                if (resolved == null) continue;

                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, playerId, enteringControllerId, enteringCard);
                for (int i = 0; i < triggerCount; i++) {
                    if (resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                        gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                                perm.getCard(), enteringControllerId, new ArrayList<>(List.of(resolved)), perm.getId(),
                                resolvedEnteringPermanentId, resolvedEnteringPermanentId));
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} any-permanent-enters trigger awaiting target", gameData.id,
                                perm.getCard().getName());
                        continue;
                    }

                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(resolved)),
                            enteringControllerId,
                            perm.getId());
                    entry.setNonTargeting(true);
                    entry.setTriggeringPermanentId(resolvedEnteringPermanentId);
                    entry.setTriggeringCardId(enteringCard.getId());
                    gameData.stack.add(entry);

                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} any-permanent-enters trigger queued", gameData.id, perm.getCard().getName());
                }
            }
        });
    }

    /** "Whenever a creature enters under an opponent's control" (ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD). */
    public void checkOpponentCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(enteringCreatureControllerId)) return;

            int extraTriggers = gameQueryService.countETBExtraTriggers(
                    gameData, playerId, enteringCreatureControllerId, enteringCreature);
            var ctx = new TriggerContext.PermanentEnters(
                    enteringCreature, enteringCreatureControllerId, enteringCreatureControllerId,
                    1 + extraTriggers, null);

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, playerId);
                    if (resolved == null) continue;
                    dispatchEnter(gameData, perm, playerId, EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx);
                }
            }
        });
    }

    /** "Whenever a land enters under an opponent's control" (ON_OPPONENT_LAND_ENTERS_BATTLEFIELD). */
    public void checkOpponentLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(landControllerId)) return;

            int extraTriggers = gameQueryService.countETBExtraTriggers(
                    gameData, playerId, landControllerId, enteringLand);
            var ctx = new TriggerContext.PermanentEnters(
                    enteringLand, landControllerId, landControllerId, 1 + extraTriggers, null);

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringLand, gameData, playerId);
                    if (resolved == null) continue;
                    resolved = unwrapImprintedCardNameConditional(gameData, enteringLand, perm, resolved);
                    if (resolved == null) continue;
                    resolved = unwrapPermanentEnteredThisTurnConditional(gameData, landControllerId, resolved);
                    if (resolved == null) continue;
                    dispatchEnter(gameData, perm, playerId, EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD, resolved, ctx);
                }
            }
        });
    }

    /**
     * "Whenever a nontoken artifact enters under your control" (ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD).
     * The entering permanent's id is preserved on any queued may-pay ability (e.g. Mirrorworks).
     */
    public void checkAllyNontokenArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (enteringCard.isToken()) return;
        if (!enteringCard.hasType(CardType.ARTIFACT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }

        int extraTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCard, controllerId, null, 1 + extraTriggers, enteringPermanentId);

        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;

                dispatchEnter(gameData, perm, controllerId, EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD, resolved, ctx);
            }
        }
    }

    /**
     * "Whenever a nontoken creature enters under your control" (ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD).
     * The entering permanent's id is preserved on any queued may-pay ability (e.g. Minion Reflector), so a
     * token-copy effect knows which creature to copy.
     */
    public void checkAllyNontokenCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (enteringCard.getToughness() == null) return;
        if (enteringCard.isToken()) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }

        int extraTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCard, controllerId, null, 1 + extraTriggers, enteringPermanentId);

        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;
                dispatchEnter(gameData, perm, controllerId, EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx);
            }
        }
    }

    /**
     * "Whenever an artifact enters under your control" (ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD).
     * Supports subtype/token gating via {@code TriggeringCardConditionalEffect} and intervening-if
     * {@code ControlsPermanentCount} (e.g. Voldaren Bloodcaster: whenever you create a Blood token,
     * if you control five or more Blood tokens, transform).
     */
    public void checkAllyArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.hasType(CardType.ARTIFACT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }
        int extraTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCard, controllerId, null, 1 + extraTriggers, enteringPermanentId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;

                // Intervening-if (CR 603.4): gate at trigger time; leave ConditionalEffect wrapped
                // so EffectResolutionService re-checks at resolution.
                if (resolved instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControlsPermanentCount) {
                    if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, controllerId))) {
                        log.info("Game {} - {} ally-artifact trigger skipped ({} not met)",
                                gameData.id, perm.getCard().getName(), conditional.condition().conditionName());
                        continue;
                    }
                }

                dispatchEnter(gameData, perm, controllerId, EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                        resolved, ctx);
            }
        }
    }

    /** Fires once for a batch of one or more tokens entering under a player's control. */
    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        if (count <= 0) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.isEmpty()) return;

        int extraTriggers = gameQueryService.countETBExtraTriggersForAnyPermanent(gameData, controllerId);
        TriggerContext.TokensEnter ctx = new TriggerContext.TokensEnter(count, 1 + extraTriggers);
        for (Permanent permanent : battlefield) {
            List<CardEffect> effects = permanent.getCard().getEffects(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                TriggerMatchContext match = new TriggerMatchContext(gameData, permanent, controllerId, effect);
                registry.dispatch(match, EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD, effect, ctx);
            }
        }
    }

    /**
     * "Whenever an Equipment enters under your control" (ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD).
     * Simple scan with no per-effect branching: each effect is put straight onto the stack.
     */
    public void checkAllyEquipmentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                for (int i = 0; i < triggerCount; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers for {} entering (ally equipment entered)",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                }
            }
        }
    }

    /**
     * "Whenever an Aura becomes attached to this creature" (ON_AURA_ATTACHED_TO_SELF, e.g. Brood
     * Keeper). Fires on the newly enchanted permanent for its own controller, regardless of who
     * controls the Aura. Call this after the Aura's {@code attachedTo} has been set.
     */
    public void checkAuraAttachedTriggers(GameData gameData, Card auraCard, UUID enchantedPermanentId) {
        if (auraCard == null || enchantedPermanentId == null) return;
        if (!auraCard.getSubtypes().contains(CardSubtype.AURA)) return;

        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null) return;
        UUID controllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        if (controllerId == null) return;

        for (CardEffect effect : enchanted.getCard().getEffects(EffectSlot.ON_AURA_ATTACHED_TO_SELF)) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    enchanted.getCard(),
                    controllerId,
                    enchanted.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    enchanted.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(enchanted.getCard()));
            log.info("Game {} - {} triggers on {} becoming attached to it",
                    gameData.id, enchanted.getCard().getName(), auraCard.getName());
        }
    }

    /**
     * "Whenever an enchantment enters under your control" (ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD).
     * Supports subtype gating via {@code TriggeringCardConditionalEffect} (e.g. Trial of Solidarity's
     * "Whenever a Cartouche you control enters").
     */
    public void checkAllyEnchantmentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.hasType(CardType.ENCHANTMENT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;

                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, controllerId, controllerId, enteringCard);

                // A permanent-targeting effect (Oath of the Ancient Wood's "+1/+1 counter on target
                // creature") can't go on the stack without a target — queue the choice instead
                // (CR 603.3d: targets are chosen as the ability is put on the stack).
                if (resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                    for (int i = 0; i < triggerCount; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                                perm.getCard(), controllerId, new ArrayList<>(List.of(resolved)), perm.getId()));
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} triggers for {} entering (ally enchantment entered, awaiting target)",
                                gameData.id, perm.getCard().getName(), enteringCard.getName());
                    }
                    continue;
                }

                for (int i = 0; i < triggerCount; i++) {
                    StackEntry triggered = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(resolved)),
                            null,
                            perm.getId()
                    );
                    // Ajani's Chosen needs the entering enchantment itself at resolution ("if that
                    // enchantment is an Aura, you may attach it to the token").
                    triggered.setTriggeringCardId(enteringCard.getId());
                    gameData.stack.add(triggered);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers for {} entering (ally enchantment entered)",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                }
            }
        }
    }

    /**
     * "Whenever you play a land" (ON_CONTROLLER_PLAYS_LAND, e.g. Search the City, Juju Bubble) and
     * its opponent-side mirror ON_OPPONENT_PLAYS_LAND (Dirtcowl Wurm). Called from the land-play
     * sites only, so a land put onto the battlefield by an effect does not trigger either.
     * Dispatches through the collector registry so name-match gates (Search the City) and bare
     * effects (Juju Bubble) both resolve correctly.
     */
    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard) {
        var ctx = new TriggerContext.LandPlayed(playingPlayerId, landCard);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(playingPlayerId)) {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_CONTROLLER_PLAYS_LAND, ctx);
            } else {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_PLAYS_LAND, ctx);
            }
        });
    }

    /**
     * "Whenever a land enters under your control" (ON_ALLY_LAND_ENTERS_BATTLEFIELD, e.g. Landfall).
     * Bundles all of a permanent's effects into a single stack entry (one landfall trigger).
     */
    public void checkAllyLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(landControllerId);
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                gameData, landControllerId, landControllerId, enteringLand);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringLand) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            List<CardEffect> resolvedEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringLand, gameData, landControllerId);
                if (resolved == null) continue;
                if (resolved instanceof ConditionalEffect conditional
                        && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, landControllerId))) {
                    log.info("Game {} - {} ally-land trigger skipped ({}) not met",
                            gameData.id, perm.getCard().getName(), conditional.conditionName());
                    continue;
                }
                resolvedEffects.add(resolved);
            }
            if (resolvedEffects.isEmpty()) continue;

            boolean needsPlayerTarget = resolvedEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            boolean needsPermanentTarget = resolvedEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (needsPlayerTarget || needsPermanentTarget) {
                for (int i = 0; i < triggerCount; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(),
                            landControllerId,
                            resolvedEffects,
                            needsPlayerTarget && !needsPermanentTarget,
                            perm.getCard().getTargetFilter(),
                            0,
                            perm.getId()
                    ));
                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                            "'s landfall ability triggers — choose a target."));
                    log.info("Game {} - {} landfall trigger queued for target selection",
                            gameData.id, perm.getCard().getName());
                }
                continue;
            }

            if (resolvedEffects.size() == 1 && resolvedEffects.getFirst() instanceof ChooseOneEffect chooseOneEffect) {
                for (int i = 0; i < triggerCount; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            perm.getCard(), landControllerId, chooseOneEffect, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on ally land entering", gameData.id, perm.getCard().getName());
                }
                continue;
            }

            for (int i = 0; i < triggerCount; i++) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        landControllerId,
                        perm.getCard().getName() + "'s ability",
                        resolvedEffects,
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally land entering", gameData.id, perm.getCard().getName());
            }
        }

        // Graveyard-resident landfall triggers (GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD, e.g. Reach of Branches).
        List<Card> graveyard = gameData.playerGraveyards.get(landControllerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = card.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringLand, gameData, landControllerId);
                    if (resolved == null) continue;

                    if (resolved instanceof MayEffect may) {
                        gameData.queueMayAbility(card, landControllerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                landControllerId,
                                card.getName() + "'s ability",
                                new ArrayList<>(List.of(resolved))
                        ));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} graveyard landfall trigger queued", gameData.id, card.getName());
                }
            }
        }
    }

    /**
     * "Whenever a creature enters from a graveyard" (ON_CREATURE_ENTERS_FROM_GRAVEYARD). Queues a
     * pending target choice rather than a stack entry, so it stays outside the registry.
     */
    public void checkEntersFromGraveyardTriggers(GameData gameData, UUID enteringControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        Permanent enteringPermanent = null;
        List<Permanent> controllerBf = gameData.playerBattlefields.get(enteringControllerId);
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard() == enteringCreature) {
                    enteringPermanent = p;
                    break;
                }
            }
        }
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null) {
            return;
        }

        UUID graveyardOwnerId = enteringPermanent.getEnteredFromGraveyardOwnerId();
        UUID enteringPermanentId = enteringPermanent.getId();

        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(graveyardOwnerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_CREATURE_ENTERS_FROM_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, playerId, enteringControllerId, enteringCreature);
                for (int i = 0; i < triggerCount; i++) {
                    gameData.queueInteraction(
                            new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                                    perm.getCard(), playerId, new ArrayList<>(List.of(effect)), enteringPermanentId));
                    gameLogService.append(gameData, GameLog.cardTextCard(perm.getCard(), "'s ability triggers (",
                            enteringCreature, " entered from a graveyard)."));
                    log.info("Game {} - {} triggers ({} entered from graveyard)",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName());
                }
            }
        });
    }

    /**
     * "Whenever this creature or another permanent enters from a graveyard"
     * (ON_PERMANENT_ENTERS_FROM_GRAVEYARD). Unlike {@link #checkEntersFromGraveyardTriggers}, fires for
     * ANY permanent (not just creatures) entering from ANY graveyard, and queues a non-targeting stack
     * entry for each source's controller rather than a target choice. Used by River Kelpie.
     */
    public void checkPermanentEntersFromGraveyardTriggers(GameData gameData, UUID enteringControllerId, Card enteringPermanentCard) {
        Permanent enteringPermanent = null;
        List<Permanent> controllerBf = gameData.playerBattlefields.get(enteringControllerId);
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard() == enteringPermanentCard) {
                    enteringPermanent = p;
                    break;
                }
            }
        }
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null) {
            return;
        }

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_PERMANENT_ENTERS_FROM_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, playerId, enteringControllerId, enteringPermanentCard);
                for (int i = 0; i < triggerCount; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.cardTextCard(perm.getCard(), "'s ability triggers (",
                            enteringPermanentCard, " entered from a graveyard)."));
                    log.info("Game {} - {} triggers ({} entered from graveyard)",
                            gameData.id, perm.getCard().getName(), enteringPermanentCard.getName());
                }
            }
        });
    }

    /**
     * "When this creature enters from a graveyard" (ON_SELF_ENTERS_FROM_GRAVEYARD). Unlike the two
     * methods above this fires only for the entering permanent's own ability. A targeting effect
     * picks its target as the ability goes on the stack (CR 603.3b) — the permanent was never cast,
     * so no target was chosen at cast time — reusing the ETB token-target pipeline with the card's
     * {@code target(...)} filter. Used by Treacherous Pit-Dweller.
     */
    public void checkSelfEntersFromGraveyardTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        List<CardEffect> effects = enteringCard.getEffects(EffectSlot.ON_SELF_ENTERS_FROM_GRAVEYARD);
        if (effects == null || effects.isEmpty()) return;

        Permanent enteringPermanent = null;
        List<Permanent> controllerBf = gameData.playerBattlefields.get(enteringControllerId);
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard() == enteringCard) {
                    enteringPermanent = p;
                    break;
                }
            }
        }
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null) {
            return;
        }

        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                gameData, enteringControllerId, enteringControllerId, enteringCard);
        for (CardEffect effect : effects) {
            boolean targets = effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
            if (targets) {
                for (int i = 0; i < triggerCount; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                            enteringCard, enteringControllerId, new ArrayList<>(List.of(effect)),
                            enteringPermanent.getId(), enteringCard.getTargetFilter()));
                }
            } else {
                for (int i = 0; i < triggerCount; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            enteringCard,
                            enteringControllerId,
                            enteringCard.getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            enteringPermanent.getId()
                    ));
                }
            }
            gameLogService.append(gameData, GameLog.cardThen(enteringCard,
                    "'s ability triggers (it entered from a graveyard)."));
            log.info("Game {} - {} triggers (it entered from a graveyard)", gameData.id, enteringCard.getName());
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
    }

    private boolean dispatchEnter(GameData gameData, Permanent perm, UUID controllerId, EffectSlot slot,
                                  CardEffect effect, TriggerContext.PermanentEnters ctx) {
        var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
        return dispatch(match, slot, effect, ctx);
    }

    private boolean dispatch(TriggerMatchContext match, EffectSlot slot, CardEffect effect,
                             TriggerContext context) {
        int previousCopies = match.gameData().beginTriggeredAbilityCopies(1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        match.gameData(), match.controllerId(), match.permanent()));
        try {
            return registry.dispatch(match, slot, effect, context);
        } finally {
            match.gameData().restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    private void collectEvolveTrigger(GameData gameData, UUID controllerId, Permanent source,
                                      Permanent enteringPermanent, int triggerCount) {
        int enteringPower = gameQueryService.getEffectivePower(gameData, enteringPermanent);
        int enteringToughness = gameQueryService.getEffectiveToughness(gameData, enteringPermanent);
        if (enteringPower <= gameQueryService.getEffectivePower(gameData, source)
                && enteringToughness <= gameQueryService.getEffectiveToughness(gameData, source)) {
            return;
        }

        for (int i = 0; i < triggerCount; i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new EvolveTriggerEffect())),
                    0,
                    source.getId());
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            entry.setTriggeringPermanentPowerAtTrigger(enteringPower);
            entry.setTriggeringPermanentToughnessAtTrigger(enteringToughness);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
            log.info("Game {} - {} triggers evolve for {} entering", gameData.id,
                    source.getCard().getName(), enteringPermanent.getCard().getName());
        }
    }

    private Permanent findPermanentByCard(GameData gameData, Card card) {
        Permanent[] found = new Permanent[1];
        gameData.forEachPermanent((playerId, permanent) -> {
            if (found[0] == null && permanent.getCard() == card) {
                found[0] = permanent;
            }
        });
        return found[0];
    }

    /**
     * Gates a stat-based enter trigger whose condition is computable from the entering creature
     * alone (e.g. Garruk's Packleader), returning the wrapped effect if it fires, {@code null} to
     * skip, or the original effect when it wasn't wrapped.
     */
    private CardEffect unwrapEnterCreatureConditional(GameData gameData, Card enteringCreature,
                                                      Permanent source, CardEffect effect) {
        if (effect instanceof EnterCreatureConditionalEffect conditional) {
            Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
            if (!conditional.testEnteringPermanent(enteringPermanent)) {
                return null;
            }
            log.info("Game {} - {} triggers for {} entering ({})",
                    gameData.id, source.getCard().getName(),
                    enteringCreature.getName(), conditional.triggerDescription(enteringCreature));
            return conditional.wrapped();
        }
        return effect;
    }

    private CardEffect unwrapOncePerTurnTrigger(GameData gameData, Permanent source, CardEffect effect) {
        if (!(effect instanceof OncePerTurnTriggerEffect once)) {
            return effect;
        }
        if (gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId())) {
            return null;
        }
        return once.wrapped();
    }

    private boolean passesEnterInterveningIf(GameData gameData, Permanent source,
                                             UUID controllerId, CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()) {
            return true;
        }
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId));
    }

    /**
     * Gates an enter trigger on the entering permanent's name matching the source's imprinted card
     * (Invader Parasite).
     */
    private CardEffect unwrapImprintedCardNameConditional(GameData gameData, Card enteringCard, Permanent source, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof ImprintedCardNameMatchesEnteringPermanent) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            ConditionContext ctx = ConditionContext.forPermanent(source, controllerId).withTriggeringCard(enteringCard);
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(), ctx)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    /**
     * Gates an enter trigger on at least {@code minCount} permanents matching a predicate having
     * entered under {@code affectedPlayerId}'s control this turn (Landfall count).
     */
    private CardEffect unwrapPermanentEnteredThisTurnConditional(GameData gameData, UUID affectedPlayerId, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof PermanentEnteredThisTurn) {
            ConditionContext ctx = new ConditionContext(affectedPlayerId, null, null, null,
                    false, false, false, false, null, 0, null, null, false);
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(), ctx)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    // ── Internal dispatch ──────────────────────────────────────────────

    private void dispatchSlot(GameData gameData, Permanent perm, UUID controllerId, EffectSlot slot, TriggerContext ctx) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
        for (CardEffect effect : perm.getCard().getEffects(slot)) {
            if (slot == EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE
                    && !passesPermanentCardPutIntoGraveyardInterveningIf(gameData, perm, controllerId, effect)) {
                continue;
            }
            var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
            dispatch(match, slot, effect, ctx);
        }
        // Triggered abilities granted continuously by another permanent (e.g. Pontiff of Blight
        // giving other creatures you control extort) fire off the permanent that has them.
        for (CardEffect effect : grantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, perm, slot)) {
            if (slot == EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE
                    && !passesPermanentCardPutIntoGraveyardInterveningIf(gameData, perm, controllerId, effect)) {
                continue;
            }
            var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
            dispatch(match, slot, effect, ctx);
        }
    }

    private boolean passesPermanentCardPutIntoGraveyardInterveningIf(GameData gameData, Permanent source,
                                                                     UUID controllerId, CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()) return true;
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId));
    }

    /**
     * Increment keyword. For each permanent the casting player controls with the {@link Keyword#INCREMENT}
     * keyword, fire the trigger if the mana spent on the cast spell is greater than that creature's current
     * power or toughness (the intervening-if; re-checked again at resolution per CR 603.4). The mana spent is
     * snapshotted into the stack entry's {@code xValue} for the resolution handler.
     */
    private void collectIncrementTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        int manaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            if (!gameQueryService.hasKeyword(gameData, perm, Keyword.INCREMENT)) return;
            if (manaSpent <= gameQueryService.getEffectivePower(gameData, perm)
                    && manaSpent <= gameQueryService.getEffectiveToughness(gameData, perm)) return;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    castingPlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new IncrementTriggerEffect())),
                    manaSpent,
                    perm.getId()
            ));
            log.info("Game {} - {} increment trigger queued (mana spent {})",
                    gameData.id, perm.getCard().getName(), manaSpent);
        });
    }

    /**
     * Unwraps triggering-card conditionals if present.
     * Returns the inner effect if the triggering card matches the predicate,
     * {@code null} if the condition is not met (caller should skip),
     * or the original effect unchanged if it wasn't wrapped.
     */
    CardEffect unwrapTriggeringCardConditional(CardEffect effect, Card triggeringCard,
                                               GameData gameData, UUID controllerId) {
        if (effect instanceof TriggeringCardConditionalEffect conditional) {
            if (!predicateEvaluationService.matchesCardPredicate(triggeringCard, conditional.predicate(), null,
                    gameData, controllerId)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    /**
     * Unwraps death-trigger conditionals that reference the dying creature's card or
     * on-battlefield characteristics (e.g. power/toughness).
     */
    CardEffect unwrapCreatureDeathConditional(CardEffect effect, Card dyingCard, Permanent dyingPermanent,
                                              GameData gameData, UUID controllerId) {
        if (effect instanceof TriggeringCardConditionalEffect conditional) {
            if (!predicateEvaluationService.matchesCardPredicate(dyingCard, conditional.predicate(), null,
                    gameData, controllerId)) {
                return null;
            }
            return conditional.wrapped();
        }
        if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
            Permanent perm = dyingPermanent != null ? dyingPermanent : new Permanent(dyingCard);
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, perm, conditional.predicate())) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    private int countCountersOnPermanent(Permanent permanent) {
        int count = 0;
        for (CounterType type : CounterType.values()) {
            if (type == CounterType.ANY || type == CounterType.SILVER) {
                continue;
            }
            count += permanent.getCounterCount(type);
        }
        return count;
    }
}
