package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSourcePreventionScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PreventDamageFromChosenSourceEffect}: collects the legal source choices
 * (optionally restricted by the effect's source filter), then starts a permanent choice whose
 * context installs the scope-appropriate prevention shield once the source is picked (see
 * {@code PermanentChoiceBattlefieldHandlerService}).
 */
@Component
@RequiredArgsConstructor
public class PreventDamageFromChosenSourceEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageFromChosenSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventDamageFromChosenSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        PermanentPredicate sourceFilter = e.sourceFilter();
        String sourceLabel = e.sourceLabel();
        if (e.sourceSharesColorWithImprintedCard()) {
            Card imprintedCard = gameData.getImprintedCard(entry.getCard());
            if (imprintedCard == null || gameData.findExiledCard(imprintedCard.getId()) == null
                    || imprintedCard.getColors() == null || imprintedCard.getColors().isEmpty()) {
                preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
                return;
            }
            sourceFilter = new PermanentColorInPredicate(Set.copyOf(imprintedCard.getColors()));
        } else if (e.sourceChosenColor()) {
            Permanent source = entry.getSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source == null) source = entry.getSourcePermanentSnapshot();
            CardColor chosenColor = source == null ? null : source.getChosenColor();
            if (chosenColor == null) {
                preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
                return;
            }
            sourceFilter = new PermanentColorInPredicate(Set.of(chosenColor));
            sourceLabel = chosenColor.name().toLowerCase(Locale.ROOT);
        } else if (e.sourceActivationManaColor()) {
            Set<CardColor> activationColors = EnumSet.noneOf(CardColor.class);
            for (ManaColor manaColor : entry.getActivationManaSpent().keySet()) {
                if (manaColor != ManaColor.COLORLESS) {
                    activationColors.add(CardColor.valueOf(manaColor.name()));
                }
            }
            sourceFilter = new PermanentColorInPredicate(activationColors);
        }

        List<UUID> validIds = e.scope() == ChosenSourcePreventionScope.NEXT_COMBAT_DAMAGE_TO_CONTROLLER_ALL_BUT_ONE
                ? collectValidBattlefieldSourceIds(gameData, sourceFilter)
                : collectValidSourceIds(gameData, sourceFilter, entry);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        String label = sourceLabel == null ? "" : sourceLabel + " ";
        PermanentChoiceContext context;
        String prompt;
        switch (e.scope()) {
            case NEXT_DAMAGE_TO_CONTROLLER -> {
                context = new PermanentChoiceContext.PreventNextDamageFromSourceChoice(
                        controllerId, e.gainLife(), e.exileFromLibrary(),
                        e.damageSourceController() ? entry.getCard() : null,
                        e.preventHalfDamage(), e.drawCards(),
                        sourceFilter instanceof PermanentColorInPredicate colorFilter
                                ? colorFilter.colors() : null,
                        sourceFilter instanceof PermanentColorInPredicate ? null : sourceFilter);
                String prevention = e.preventHalfDamage()
                        ? "prevent half that damage, rounded down"
                        : "prevent that damage";
                String rider;
                if (e.damageSourceController() && e.drawCards()) {
                    rider = " If damage is prevented this way, this spell deals that much damage to that source's controller"
                            + " and you draw that many cards.";
                } else if (e.damageSourceController()) {
                    rider = " If damage is prevented this way, this spell deals that much damage to that source's controller.";
                } else if (e.gainLife()) {
                    rider = " and gain that much life.";
                } else if (e.exileFromLibrary()) {
                    rider = " and exile that many cards from the top of your library.";
                } else {
                    rider = ".";
                }
                prompt = "Choose a " + label
                        + "source. The next time it would deal damage to you this turn, " + prevention
                        + rider;
            }
            case NEXT_COMBAT_DAMAGE_TO_CONTROLLER_ALL_BUT_ONE -> {
                context = new PermanentChoiceContext.PreventNextCombatDamageFromUnblockedCreatureChoice(controllerId);
                prompt = "Choose a creature. The next time it is unblocked and would deal combat damage to you this turn, prevent all but 1 of that damage.";
            }
            case NEXT_DAMAGE_TO_ANY_TARGET -> {
                context = e.damageRedSourceController()
                        ? new PermanentChoiceContext.PreventNextDamageFromSourceToAnyTargetChoice(
                                controllerId, true, entry.getCard(), sourceFilter)
                        : new PermanentChoiceContext.PreventNextDamageFromSourceToAnyTargetChoice(
                                controllerId, false, null, sourceFilter);
                prompt = e.damageRedSourceController()
                        ? "Choose a source. The next time it would deal damage to any target this turn, prevent that damage."
                                + " If it is red, Honorable Passage deals that much damage to its controller."
                        : "Choose a source. The next time it would deal damage to any target this turn, prevent that damage.";
            }
            case NEXT_DAMAGE_TO_CONTROLLER_AND_CREATURES -> {
                context = new PermanentChoiceContext.PreventNextDamageFromSourceToYouAndYourCreaturesChoice(controllerId);
                prompt = "Choose a source. The next time it would deal damage to you and/or creatures you control"
                        + " this turn, prevent that damage. If that source is black, you gain that much life.";
            }
            case NEXT_DAMAGE_TO_ENCHANTED -> {
                UUID enchantedId = findEnchantedCreatureId(gameData, entry);
                if (enchantedId == null) {
                    return;
                }
                context = new PermanentChoiceContext.PreventNextDamageFromSourceToPermanentChoice(
                        controllerId, enchantedId);
                prompt = "Choose a source. The next time it would deal damage to enchanted creature this turn,"
                        + " prevent that damage.";
            }
            case NEXT_DAMAGE_TO_TARGET_CREATURE -> {
                UUID targetId = entry.getTargetId();
                if (targetId == null) {
                    return;
                }
                context = new PermanentChoiceContext.PreventNextDamageFromSourceToPermanentChoice(
                        controllerId, targetId);
                prompt = "Choose a source. The next time it would deal damage to target creature this turn,"
                        + " prevent that damage.";
            }
            case NEXT_DAMAGE_TO_TARGET -> {
                UUID targetId = entry.getTargetId();
                if (targetId == null) {
                    return;
                }
                context = new PermanentChoiceContext.PreventNextDamageFromSourceToPermanentChoice(
                        controllerId, targetId);
                prompt = "Choose a source. The next time it would deal damage to the target this turn,"
                        + " prevent that damage.";
            }
            case ALL_DAMAGE_THIS_TURN -> {
                context = new PermanentChoiceContext.PreventDamageSourceChoice(
                        controllerId, e.controllerOnly(), e.gainLifeForBlackOrRedSource(),
                        sourceFilter instanceof PermanentColorInPredicate colorFilter
                                ? colorFilter.colors() : Set.of());
                prompt = e.sourceActivationManaColor()
                        ? "Choose a source that shares a color with the mana spent on this activation. Prevent all damage it would deal "
                                + (e.controllerOnly() ? "to you" : "") + " this turn."
                        : e.controllerOnly()
                        ? "Choose a source. Prevent all damage it would deal to you this turn."
                        : e.sourceSharesColorWithImprintedCard()
                                ? "Choose a source that shares a color with the exiled card. Prevent all damage it would deal this turn."
                                : "Choose a " + label + "source. Prevent all damage it would deal this turn.";
            }
            default -> throw new IllegalStateException("Unknown chosen-source prevention scope: " + e.scope());
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds, prompt);
    }

    /**
     * Resolves "enchanted creature" for the ability's source Aura. The Aura is usually sacrificed to
     * pay the activation cost, so the live permanent is gone by resolution; the attachment is read
     * from the last-known snapshot (CR 608.2h) with a live lookup as fallback.
     */
    private UUID findEnchantedCreatureId(GameData gameData, StackEntry entry) {
        UUID sourceId = entry.getSourcePermanentId();
        Permanent aura = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        UUID attachedTo = aura != null ? aura.getAttachedTo() : null;
        if (attachedTo == null && entry.getSourcePermanentSnapshot() != null) {
            attachedTo = entry.getSourcePermanentSnapshot().getAttachedTo();
        }
        if (attachedTo == null) {
            return null;
        }
        return gameQueryService.findPermanentById(gameData, attachedTo) == null ? null : attachedTo;
    }

    private List<UUID> collectValidSourceIds(GameData gameData, PermanentPredicate sourceFilter, StackEntry resolvingEntry) {
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (sourceFilter == null
                    || predicateEvaluationService.matchesPermanentPredicate(gameData, perm, sourceFilter)) {
                validIds.add(perm.getId());
            }
        });
        // CR 609.7a: the resolving ability refers to its own source, which may already have left the
        // battlefield (e.g. sacrificed as a cost); its last known information is used (CR 113.7a).
        if (!isSpell(resolvingEntry.getEntryType()) && resolvingEntry.getSourcePermanentSnapshot() != null) {
            Permanent abilitySource = resolvingEntry.getSourcePermanentSnapshot();
            if (!validIds.contains(abilitySource.getId()) && (sourceFilter == null
                    || predicateEvaluationService.matchesPermanentPredicate(gameData, abilitySource, sourceFilter))) {
                validIds.add(abilitySource.getId());
            }
        }
        for (StackEntry stackEntry : gameData.stack) {
            // CR 609.7a: any object referred to by an object on the stack is a legal source, even
            // while it sits in a graveyard or exile.
            List<UUID> referencedIds = new ArrayList<>(stackEntry.getTargetIds());
            if (stackEntry.getTargetId() != null) referencedIds.add(stackEntry.getTargetId());
            referencedIds.addAll(stackEntry.getTargetCardIds());
            for (UUID referencedId : referencedIds) {
                Card referencedCard = gameQueryService.findCardInGraveyardById(gameData, referencedId);
                if (referencedCard == null && gameData.findExiledCard(referencedId) != null) {
                    referencedCard = gameData.findExiledCard(referencedId).card();
                }
                if (referencedCard == null) {
                    referencedCard = stackEntry.lastKnownPermanentCard(referencedId);
                }
                if (referencedCard == null) {
                    referencedCard = findDepartedAbilitySourceCard(gameData, resolvingEntry, referencedId);
                }
                if (referencedCard == null) {
                    referencedCard = findDepartedPermanentCard(gameData, referencedId);
                }
                if (referencedCard != null && !validIds.contains(referencedId)
                        && (sourceFilter == null || predicateEvaluationService.matchesPermanentPredicate(
                        gameData, new Permanent(referencedCard), sourceFilter))) {
                    validIds.add(referencedId);
                }
            }
            boolean spell = isSpell(stackEntry.getEntryType());
            // An ability whose source never was a permanent (a trigger of a discarded card) is sourced by the card.
            boolean cardSourcedAbility = !spell && stackEntry.getSourcePermanentSnapshot() == null
                    && stackEntry.getSourcePermanentId() == null
                    && stackEntry.getCard() != null;
            Permanent source = spell || cardSourcedAbility ? new Permanent(stackEntry.getCard())
                    : stackEntry.getSourcePermanentSnapshot();
            if (source == null) continue;
            if (sourceFilter == null
                    || predicateEvaluationService.matchesPermanentPredicate(gameData, source, sourceFilter)) {
                UUID sourceId = spell ? stackEntry.getTargetableId()
                        : cardSourcedAbility ? stackEntry.getCard().getId() : source.getId();
                if (!validIds.contains(sourceId)) validIds.add(sourceId);
            }
        }
        // CR 609.7a: a source referred to by a prevention effect that is still waiting to apply stays a
        // legal choice after it has left the battlefield (its card is kept with the shield).
        for (PlayerSourceNextDamageShield shield : gameData.playerSourceNextDamageShields) {
            UUID referencedId = shield.sourceId();
            Card referencedCard = gameData.waitingRedirectDamageSourceCards.get(referencedId);
            if (referencedCard != null && !validIds.contains(referencedId)
                    && (sourceFilter == null || predicateEvaluationService.matchesPermanentPredicate(
                    gameData, new Permanent(referencedCard), sourceFilter))) {
                validIds.add(referencedId);
            }
        }
        return validIds;
    }

    private List<UUID> collectValidBattlefieldSourceIds(GameData gameData, PermanentPredicate sourceFilter) {
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (sourceFilter == null
                    || predicateEvaluationService.matchesPermanentPredicate(gameData, perm, sourceFilter)) {
                validIds.add(perm.getId());
            }
        });
        return validIds;
    }

    /**
     * Last-known card of a permanent that left the battlefield while it was the source of an ability
     * (on the stack or the resolving entry), matched by the permanent's id as a stack reference holds it.
     */
    private static Card findDepartedPermanentCard(GameData gameData, UUID permanentId) {
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> snapshot.getId().equals(permanentId))
                .map(Permanent::getCard)
                .findFirst()
                .orElse(null);
    }

    private Card findDepartedAbilitySourceCard(GameData gameData, StackEntry resolvingEntry, UUID permanentId) {
        if (matchesAbilitySourceSnapshot(resolvingEntry, permanentId)) {
            return resolvingEntry.getSourcePermanentSnapshot().getCard();
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (matchesAbilitySourceSnapshot(stackEntry, permanentId)) {
                return stackEntry.getSourcePermanentSnapshot().getCard();
            }
        }
        return null;
    }

    private static boolean matchesAbilitySourceSnapshot(StackEntry entry, UUID permanentId) {
        return !isSpell(entry.getEntryType()) && entry.getSourcePermanentSnapshot() != null
                && permanentId.equals(entry.getSourcePermanentSnapshot().getId());
    }

    private static boolean isSpell(StackEntryType type) {
        return type != StackEntryType.ACTIVATED_ABILITY && type != StackEntryType.TRIGGERED_ABILITY;
    }
}
