package com.github.laxika.magicalvibes.service.aura;

import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.UnattachTriggerSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Handles aura and attachment lifecycle: removing orphaned or unattached auras and detaching
 * equipment whose host leaves the battlefield. After each cleanup the CR 613.1b control state is
 * reconciled ({@link CreatureControlService#reconcileControl}) so permanents whose controlling
 * effect ended fall back to the next most recent still-active control effect (or their owner).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuraAttachmentService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final UnattachTriggerSupport unattachTriggerSupport;

    /**
     * A card that was put into the graveyard as an orphaned aura, along with the controller
     * who owned it at the time. Callers use this to fire graveyard triggers after cleanup.
     */
    public record OrphanedAuraRemoval(Card card, UUID controllerId) {}

    /**
     * Removes auras that are unattached or whose enchanted permanent no longer exists, and
     * detaches equipment whose equipped creature has left the battlefield (CR 704.5m,
     * CR 301.5c). Orphaned auras are put into their owner's graveyard; equipment simply becomes
     * unattached. After cleanup, the layer-2 control state is reconciled so permanents whose
     * controlling effect has ended change controllers accordingly.
     *
     * <p>"No longer exists" includes a host that phased out: a phased-out permanent is treated
     * as though it does not exist (CR 702.26b), so an attachment that was kept from following it
     * out — Spatial Binding's "target permanent can't phase out" — is orphaned here rather than
     * waiting for the host to phase back in. That is the official Spatial Binding ruling: the
     * Aura stays on the battlefield and is then immediately put into the graveyard.</p>
     *
     * @param gameData the current game state
     * @return what the sweep changed, so the SBA loop knows whether to run another pass
     */
    public AttachmentSweepResult removeOrphanedAuras(GameData gameData) {
        List<OrphanedAuraRemoval> removals = new ArrayList<>();
        boolean anyUnattached = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                boolean isAura = p.getCard().getSubtypes().contains(CardSubtype.AURA);
                boolean attachmentIsMissing = p.isAttached()
                        && !gameData.playerIds.contains(p.getAttachedTo())
                        && gameQueryService.findPermanentById(gameData, p.getAttachedTo()) == null;
                if ((isAura && !p.isAttached()) || attachmentIsMissing) {
                    if (p.isBestow()) {
                        p.setCard(p.getOriginalCard());
                        p.setBestow(false);
                        p.setAttachedTo(null);
                        gameData.expireFloatingEffectsForUnattachedSource(p.getId());
                        anyUnattached = true;
                        gameLogService.append(gameData, GameLog.cardThen(p.getCard(), " becomes an enchantment creature (bestow attachment ended)."));
                        log.info("Game {} - {} becomes a creature after bestow attachment ended", gameData.id, p.getCard().getName());
                    } else if (p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                        // Equipment stays on the battlefield unattached when the equipped creature leaves
                        unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, p, p.getAttachedTo());
                        p.setAttachedTo(null);
                        gameData.expireFloatingEffectsForUnattachedSource(p.getId());
                        anyUnattached = true;
                        
                        gameLogService.append(gameData, GameLog.cardThen(p.getCard(), " becomes unattached (equipped creature left the battlefield)."));
                        log.info("Game {} - {} unattached (equipped creature left)", gameData.id, p.getCard().getName());
                    } else {
                        boolean hadOilCounter = p.getCounterCount(CounterType.OIL) > 0;
                        it.remove();
                        gameData.expireFloatingEffectsForDepartedSource(p.getId());
                        boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, playerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                        if (wentToGraveyard && hadOilCounter) {
                            gameData.recordPermanentWithOilCounterPutIntoGraveyard();
                        }
                        String reason = p.isAttached()
                                ? "enchanted permanent left the battlefield"
                                : "Aura is not attached";
                        gameLogService.append(gameData, GameLog.cardThen(p.getCard(), " is put into the graveyard (" + reason + ")."));
                        log.info("Game {} - {} removed (orphaned aura)", gameData.id, p.getCard().getName());
                        if (wentToGraveyard) {
                            removals.add(new OrphanedAuraRemoval(p.getCard(), playerId));
                        }
                    }
                }
            }
        }
        creatureControlService.reconcileControl(gameData);
        return new AttachmentSweepResult(removals, anyUnattached);
    }

    /**
     * Outcome of one attachment sweep — {@link #removeOrphanedAuras} or
     * {@link #enforceAttachmentLegality}: auras put into the graveyard (for the caller to fire
     * graveyard triggers) and whether any equipment became unattached.
     */
    public record AttachmentSweepResult(List<OrphanedAuraRemoval> removals, boolean anyUnattached) {
        public boolean anyChange() {
            return anyUnattached || !removals.isEmpty();
        }
    }

    /**
     * Enforces ongoing attachment legality for auras and equipment whose attached object is
     * still on the battlefield (the departed-object case is {@link #removeOrphanedAuras}):
     * an aura attached to an object it can't legally enchant — the object has protection from
     * it, or no longer satisfies the card's enchant restriction (its declared target filter) —
     * is put into its owner's graveyard (CR 704.5m); equipment attached to a permanent that is
     * not a creature or has protection from it becomes unattached (CR 704.5n).
     */
    public AttachmentSweepResult enforceAttachmentLegality(GameData gameData) {
        List<OrphanedAuraRemoval> removals = new ArrayList<>();
        boolean anyUnattached = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (!p.isAttached()) continue;
                boolean isAura = GameQueryService.permanentHasSubtype(p, CardSubtype.AURA);
                boolean isEquipment = GameQueryService.permanentHasSubtype(p, CardSubtype.EQUIPMENT);
                if (!isAura && !isEquipment) {
                    // CR 704.5p — neither Aura, Equipment, nor Fortification may stay attached
                    unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, p, p.getAttachedTo());
                    p.setAttachedTo(null);
                    gameData.expireFloatingEffectsForUnattachedSource(p.getId());
                    anyUnattached = true;
                    gameLogService.append(gameData, GameLog.builder().card(p.getCard())
                            .text(" becomes unattached (it is no longer an Aura or Equipment).").build());
                    log.info("Game {} - {} unattached (no longer Aura/Equipment)", gameData.id, p.getCard().getName());
                    continue;
                }

                String reason = illegalAttachmentReason(gameData, p, playerId, isAura);
                if (reason == null) continue;

                if (isEquipment) {
                    // CR 704.5n — illegally attached equipment becomes unattached but stays
                    unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, p, p.getAttachedTo());
                    p.setAttachedTo(null);
                    gameData.expireFloatingEffectsForUnattachedSource(p.getId());
                    anyUnattached = true;
                    
                    gameLogService.append(gameData, GameLog.builder().card(p.getCard()).text(" becomes unattached (" + reason + ").").build());
                    log.info("Game {} - {} unattached ({})", gameData.id, p.getCard().getName(), reason);
                } else if (p.isBestow()) {
                    p.setCard(p.getOriginalCard());
                    p.setBestow(false);
                    p.setAttachedTo(null);
                    gameData.expireFloatingEffectsForUnattachedSource(p.getId());
                    anyUnattached = true;
                    gameLogService.append(gameData, GameLog.cardThen(p.getCard(), " becomes an enchantment creature (bestow attachment became illegal)."));
                    log.info("Game {} - {} becomes a creature after illegal bestow attachment", gameData.id, p.getCard().getName());
                } else {
                    // CR 704.5m — an illegally attached aura is put into its owner's graveyard
                    boolean hadOilCounter = p.getCounterCount(CounterType.OIL) > 0;
                    it.remove();
                    gameData.expireFloatingEffectsForDepartedSource(p.getId());
                    boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, playerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                    if (wentToGraveyard && hadOilCounter) {
                        gameData.recordPermanentWithOilCounterPutIntoGraveyard();
                    }
                    
                    gameLogService.append(gameData, GameLog.builder().card(p.getCard()).text(" is put into the graveyard (" + reason + ").").build());
                    log.info("Game {} - {} removed (illegally attached: {})", gameData.id, p.getCard().getName(), reason);
                    if (wentToGraveyard) {
                        removals.add(new OrphanedAuraRemoval(p.getCard(), playerId));
                    }
                }
            }
        }
        if (anyUnattached || !removals.isEmpty()) {
            creatureControlService.reconcileControl(gameData);
        }
        return new AttachmentSweepResult(removals, anyUnattached);
    }

    /**
     * Whether {@code auraCard} could enchant {@code host} (CR 701.3a — an Aura can't be attached to
     * an object it couldn't enchant). The card's declared target filter is its enchant restriction;
     * an Aura without one enchants creatures. Used by effects that move or put Auras onto a chosen
     * permanent, where the restriction has to be checked before the Aura is offered as a choice.
     *
     * @param auraControllerId the controller of the Aura, for controller-relative enchant filters
     */
    public boolean canEnchant(GameData gameData, Card auraCard, UUID auraControllerId, Permanent host) {
        TargetFilter filter = auraCard.getDeclaredTargetFilter();
        if (filter == null) {
            return gameQueryService.isCreature(gameData, host);
        }
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(auraCard.getId())
                .withSourceControllerId(auraControllerId);
        return predicateEvaluationService.checkTargetFilter(filter, host, context).isEmpty();
    }

    /**
     * Why the attachment is illegal on its current host, or {@code null} while it is legal.
     * Hexproof and shroud are deliberately not checked — they restrict targeting, not staying
     * attached. Equipment ignores the equip ability's "creature you control" restriction:
     * attachment only requires the host to be a creature (CR 301.5c) and, when the card declares
     * one, to satisfy its {@code attachRestriction} ("can be attached only to a legendary
     * creature").
     */
    private String illegalAttachmentReason(GameData gameData, Permanent attachment, UUID controllerId, boolean isAura) {
        UUID attachedTo = attachment.getAttachedTo();
        if (gameData.playerIds.contains(attachedTo)) {
            // Aura enchanting a player (curse-style): illegal while the player has protection
            // from one of the aura's colors
            if (isAura) {
                if (gameQueryService.playerHasProtectionFromEverything(gameData, attachedTo)) {
                    return "enchanted player has protection from everything";
                }
                for (CardColor color : gameQueryService.getEffectiveColors(gameData, attachment)) {
                    if (gameQueryService.playerHasProtectionFromColor(gameData, attachedTo, color)) {
                        return "enchanted player has protection from it";
                    }
                }
            }
            return null;
        }
        Permanent host = gameQueryService.findPermanentById(gameData, attachedTo);
        if (host == null) {
            // Departed host: removeOrphanedAuras owns that cleanup
            return null;
        }

        if (!grantsSelfExemptProtection(attachment)
                && gameQueryService.hasProtectionFromSource(gameData, host, attachment)) {
            return (isAura ? "enchanted" : "equipped") + " permanent has protection from it";
        }

        if (!isAura) {
            if (gameQueryService.cantBeEquipped(gameData, host)) {
                return "equipped permanent can't be equipped";
            }
            // CR 301.5c — an Equipment that's also a creature can't equip a creature
            if (gameQueryService.isCreature(gameData, attachment)) {
                return "it is a creature";
            }
            if (!gameQueryService.isCreature(gameData, host)) {
                return "equipped permanent is no longer a creature";
            }
            PermanentPredicate attachRestriction = attachment.getCard().getAttachRestriction();
            if (attachRestriction != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, host, attachRestriction)) {
                return "it can be attached only to a legal permanent";
            }
            return null;
        }

        // The card's declared target filter is its enchant restriction (e.g. "Enchant creature");
        // player-shaped filters never apply to a permanent host.
        TargetFilter filter = attachment.getCard().getDeclaredTargetFilter();
        if (filter instanceof PermanentPredicateTargetFilter
                || filter instanceof ControlledPermanentPredicateTargetFilter
                || filter instanceof OwnedPermanentPredicateTargetFilter) {
            FilterContext context = new FilterContext(gameData, attachment.getCard().getId(), controllerId, null, null);
            if (predicateEvaluationService.checkTargetFilter(filter, host, context).isPresent()) {
                return "it can no longer enchant " + host.getCard().getName();
            }
        }
        return null;
    }

    /**
     * Auras whose protection grant explicitly keeps the Aura attached even when the Aura itself is
     * among the protected sources. The fixed-color form is used by Spectra Ward, the chosen-color
     * form by Ward of Lights, and the card-type form by Tattoo Ward.
     */
    private boolean grantsSelfExemptProtection(Permanent attachment) {
        return attachment.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effect -> {
                    if (effect instanceof ProtectionFromChosenColorEffect chosenProtection) {
                        return chosenProtection.scope() == GrantScope.ENCHANTED_CREATURE;
                    }
                    if (effect instanceof ProtectionFromColorsEffect fixedProtection) {
                        return fixedProtection.scope() == GrantScope.ENCHANTED_CREATURE;
                    }
                    if (effect instanceof GrantEffectEffect grant
                            && grant.scope() == GrantScope.ENCHANTED_CREATURE
                            && grant.effect() instanceof ProtectionFromCardTypesEffect cardTypeProtection) {
                        return cardTypeProtection.cardTypes().contains(CardType.ENCHANTMENT);
                    }
                    if (effect instanceof ProtectionFromColorsOfPermanentsYouControlEffect dynamicProtection) {
                        return dynamicProtection.scope() == GrantScope.ENCHANTED_CREATURE;
                    }
                    return false;
                });
    }
}
