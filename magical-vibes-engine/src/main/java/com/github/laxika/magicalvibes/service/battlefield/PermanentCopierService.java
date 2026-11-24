package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ChooseIndependentModesOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ResolvedCopyEffect;
import com.github.laxika.magicalvibes.model.effect.LicidEndEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class PermanentCopierService {

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride, Integer toughnessOverride) {
        applyCloneCopy(clonePerm, targetPerm, powerOverride, toughnessOverride, Set.of());
    }

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride,
                                Integer toughnessOverride, Set<CardType> additionalTypesOverride) {
        applyCloneCopy(clonePerm, copiableCard(targetPerm), powerOverride, toughnessOverride, additionalTypesOverride);
    }

    /** The copiable values of {@code permanent} (CR 707.2): its card as copy and as-enters effects left it. */
    public Card copiableCard(Permanent permanent) {
        return printedCopiableCard(permanent);
    }

    /**
     * The copiable values of face-down {@code permanent}: only the characteristics its face-down status lists
     * (CR 707.2, 708.2, 708.2a), so no name, mana cost, text or printing of the hidden card. A disguised or cloaked
     * one has ward {2} (CR 702.168a, 701.58a). A copy of it is face up (CR 707.2). A face-down permanent that is
     * itself a copy keeps these values too (CR 708.10).
     */
    public static Card faceDownCopiableCard(Permanent permanent) {
        Card visible = new Card();
        visible.setName("");
        Set<CardType> types = permanent.getFaceDownCardTypes().isEmpty()
                ? EnumSet.of(CardType.CREATURE) : EnumSet.copyOf(permanent.getFaceDownCardTypes());
        CardType primary = types.contains(CardType.CREATURE) ? CardType.CREATURE : types.iterator().next();
        types.remove(primary);
        visible.setType(primary);
        visible.setAdditionalTypes(types);
        visible.setSubtypes(List.copyOf(permanent.getFaceDownSubtypes()));
        visible.setManaCost("");
        visible.setPower(permanent.getFaceDownPower());
        visible.setToughness(permanent.getFaceDownToughness());
        if (permanent.isCloaked() || permanent.isFaceDownWard()) {
            withWardTwo(visible);
        }
        return visible;
    }

    /** Gives {@code card} ward {2}: the keyword and its triggered ability (CR 702.21a). */
    public static Card withWardTwo(Card card) {
        card.setKeywords(EnumSet.of(Keyword.WARD));
        card.addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
        return card;
    }

    /**
     * The card a copy of {@code permanent}, live or as it last existed, copies when the copying effect reads the
     * permanent's card: the face-down copiable values while it is face down ({@link #faceDownCopiableCard}), else its
     * card.
     */
    public static Card cardToCopy(Permanent permanent) {
        return permanent.isFaceDown() ? faceDownCopiableCard(permanent) : permanent.getCard();
    }

    /**
     * The card a copy of {@code permanentId}, which has left the battlefield, copies when the effect recorded
     * {@code lastKnownCard} for it: the face-down copiable values when it was face down as it last existed there
     * (CR 608.2h, 707.2), as {@code entry}'s or the game's last-known snapshots of it show, else {@code lastKnownCard}.
     */
    public static Card lastKnownCardToCopy(GameData gameData, StackEntry entry, UUID permanentId, Card lastKnownCard) {
        if (lastKnownCard == null || permanentId == null) {
            return lastKnownCard;
        }
        Permanent lastKnown = java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(entry.getTriggeringPermanentSnapshot(),
                                entry.getSourcePermanentSnapshot(), entry.getSacrificedPermanentSnapshot()),
                        gameData.departedPermanentSnapshots.values().stream())
                .filter(java.util.Objects::nonNull)
                .filter(snapshot -> permanentId.equals(snapshot.getId()))
                .findFirst()
                .orElse(null);
        return lastKnown != null && lastKnown.isFaceDown() ? faceDownCopiableCard(lastKnown) : lastKnownCard;
    }

    /**
     * The copiable values of {@code permanentId} as it last existed on the battlefield (CR 608.2h, 707.2), once it has
     * left: those of its departure snapshot, else of the snapshot {@code entry} recorded of it (triggering, source,
     * sacrificed or attached permanent), else {@code entry}'s last-known card of it; {@code null} when none is known.
     * The snapshot's card is the one copy effects left it, not the one other continuous effects made (its
     * last-known card), so a copy of it has no counters, Aura grants or anthem keywords.
     */
    public Card lastKnownCopiableCard(GameData gameData, StackEntry entry, UUID permanentId) {
        Permanent lastKnown = java.util.stream.Stream.concat(
                        gameData.departedPermanentSnapshots.values().stream(),
                        java.util.stream.Stream.of(entry.getTriggeringPermanentSnapshot(),
                                entry.getSourcePermanentSnapshot(), entry.getSacrificedPermanentSnapshot(),
                                entry.getAttachedPermanentSnapshot()))
                .filter(java.util.Objects::nonNull)
                .filter(snapshot -> permanentId.equals(snapshot.getId()))
                .findFirst()
                .orElse(null);
        return lastKnown != null ? copiableCard(lastKnown) : entry.lastKnownPermanentCard(permanentId);
    }

    private Card printedCopiableCard(Permanent permanent) {
        if (permanent.isFaceDown()) {
            return faceDownCopiableCard(permanent);
        }
        // The Aura characteristics a bestowed creature has are type-changing, so they are not copiable (CR 707.2).
        Card original = permanent.getBecomeCreatureBaseCard() != null
                && permanent.getBecomeCreatureResultCard() == permanent.getCard()
                ? permanent.getBecomeCreatureBaseCard()
                : permanent.isBestow() ? permanent.getOriginalCard() : permanent.getCard();
        if (permanent.getFullTextCopyBaseCard() != null) {
            // Text borrowed from the graveyard (Volrath's Shapeshifter) is not copiable (CR 707.2).
            original = permanent.getFullTextCopyBaseCard();
        }
        if (original.getActivatedAbilities(Zone.BATTLEFIELD).stream().anyMatch(ability -> ability.getEffects().stream()
                .anyMatch(LicidEndEffect.class::isInstance))) {
            // A Licid that became an Aura: that is the effect of its ability, not a copiable value, so a
            // copy sees the printed creature form (CR 707.2).
            return permanent.getOriginalCard();
        }
        if (original.getStaticEffects().stream()
                .noneMatch(ChooseIndependentModesOnEnterEffect.class::isInstance)) {
            return original;
        }
        Card copy = original.createRuntimeCopy();
        for (CardEffect effect : original.getStaticEffects()) {
            CardEffect chosen = chosenModeEffect(copy, permanent, effect);
            if (chosen != null) {
                copy.addStaticEffect(chosen);
            }
        }
        for (TriggeredAbility ability : original.getTriggeredAbilities()) {
            for (EffectRegistration registration : ability.effects()) {
                CardEffect chosen = chosenModeEffect(copy, permanent, registration.effect());
                if (chosen != null) {
                    copy.addTriggeredAbility(ability.withEffects(List.of(
                            new EffectRegistration(chosen))));
                }
            }
        }
        return copy;
    }

    /**
     * The effect that {@code effect} becomes on a copy when it applies only in a mode the copied
     * permanent chose, or {@code null} when it doesn't depend on a chosen mode. A chosen base
     * power and toughness is set on {@code copy} instead, so it is {@code null} too.
     */
    private static CardEffect chosenModeEffect(Card copy, Permanent permanent, CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof SourceHasChosenMode chosenMode
                && permanent.getChosenModeLabels().contains(chosenMode.mode()))) {
            return null;
        }
        if (conditional.wrapped() instanceof SetBasePowerToughnessEffect set && set.subject() instanceof PermanentSubject.Source) {
            copy.setPower(set.power());
            copy.setToughness(set.toughness());
            return null;
        }
        return conditional.wrapped();
    }

    /**
     * Copies directly from a {@link Card} rather than a live {@link Permanent}, so a copy can be built
     * from last-known information (e.g. a creature that died — Cemetery Puca) without wrapping the
     * source card in a Permanent (which would freeze it).
     */
    public void applyCloneCopy(Permanent clonePerm, Card target, Integer powerOverride,
                                Integer toughnessOverride, Set<CardType> additionalTypesOverride) {
        applyCloneCopy(clonePerm, target, powerOverride, toughnessOverride, additionalTypesOverride, List.of(), true);
    }

    public void applyCloneCopy(Permanent clonePerm, Card target, Integer powerOverride,
                               Integer toughnessOverride, Set<CardType> additionalTypesOverride,
                               List<ActivatedAbility> retainedAbilities) {
        applyCloneCopy(clonePerm, target, powerOverride, toughnessOverride, additionalTypesOverride,
                retainedAbilities, true);
    }

    public void applyCloneCopy(Permanent clonePerm, Card target, Integer powerOverride,
                               Integer toughnessOverride, Set<CardType> additionalTypesOverride,
                               List<ActivatedAbility> retainedAbilities, boolean copyColor) {
        clonePerm.setFullTextCopyBaseCard(null);
        clonePerm.setFullTextCopySourceCard(null);
        Card copy = new Card();
        copy.setName(target.getName());
        copy.setType(target.getType());
        copy.setAdditionalTypes(target.getAdditionalTypes());
        copy.setManaCost(target.getManaCost());
        Card currentCard = clonePerm.getCard();
        copy.setColor(copyColor ? target.getColor() : currentCard.getColor());
        copy.setColors(copyColor ? target.getColors() : currentCard.getColors());
        copy.setSupertypes(target.getSupertypes());
        copy.setSubtypes(target.getSubtypes());
        copy.setCardText(target.getCardText());
        copy.copyTargetingFrom(target);
        copy.setPower(powerOverride != null ? powerOverride : target.getPower());
        copy.setToughness(toughnessOverride != null ? toughnessOverride : target.getToughness());
        copy.setLoyalty(target.getLoyalty());
        copy.setToken(clonePerm.getCard().isToken());
        copy.setKeywords(target.getKeywords());
        copy.setSetCode(target.getSetCode());
        copy.setCollectorNumber(target.getCollectorNumber());
        boolean hasPTOverride = powerOverride != null || toughnessOverride != null;
        copy.addCastingCostsAndSpellEffectsFrom(target,
                effect -> !hasPTOverride || !effect.isPowerToughnessDefining());
        // CR 707.9d: when a copy effect provides specific P/T values,
        // characteristic-defining abilities that define P/T are not copied
        copy.addStaticAbilitiesFrom(target, effect -> !hasPTOverride || !effect.isPowerToughnessDefining());
        for (TriggeredAbility ability : target.getTriggeredAbilities()) {
            if (!hasPTOverride || ability.effects().stream()
                    .noneMatch(reg -> reg.effect().isPowerToughnessDefining())) {
                copy.addTriggeredAbility(ability);
            }
        }
        for (ActivatedAbility ability : target.getActivatedAbilities(Zone.BATTLEFIELD)) {
            copy.addActivatedAbility(ability);
        }
        for (ActivatedAbility ability : retainedAbilities) {
            copy.addActivatedAbility(ability);
        }

        if (additionalTypesOverride != null && !additionalTypesOverride.isEmpty()) {
            Set<CardType> merged = EnumSet.noneOf(CardType.class);
            merged.addAll(copy.getAdditionalTypes());
            for (CardType overrideType : additionalTypesOverride) {
                if (overrideType != copy.getType() && !merged.contains(overrideType)) {
                    merged.add(overrideType);
                }
            }
            copy.setAdditionalTypes(merged);
        }

        clonePerm.setCard(copy);
    }

    public void applyCardTypesOverride(Permanent permanent, Set<CardType> cardTypesOverride) {
        if (cardTypesOverride == null || cardTypesOverride.isEmpty()) {
            return;
        }

        EnumSet<CardType> exactTypes = EnumSet.copyOf(cardTypesOverride);
        CardType primaryType = exactTypes.iterator().next();
        exactTypes.remove(primaryType);
        permanent.getCard().setType(primaryType);
        permanent.getCard().setAdditionalTypes(exactTypes);
    }

    /**
     * The card {@code permanent} showed before its active copy effects from resolutions: the one they keep, or its
     * current card when it has none (the card a new such copy keeps).
     */
    public static Card preCopyCard(GameData gameData, Permanent permanent) {
        List<FloatingContinuousEffect> copies = resolvedCopies(gameData, permanent.getId());
        return copies.isEmpty() ? permanent.getCard()
                : ((ResolvedCopyEffect) copies.getFirst().effect()).preCopyCard();
    }

    /**
     * Records the copy just applied to {@code permanent} as a layer-1 floating effect (CR 613.1a) with
     * {@code duration}, made by {@code sourceCardName}'s {@code sourcePermanentId} (whose state a "for as long as"
     * duration follows). It keeps the card the permanent now shows and {@code preCopyCard}, from {@link #preCopyCard}
     * before the copy applied.
     */
    public static void beginCopy(GameData gameData, String sourceCardName, UUID sourcePermanentId, UUID controllerId,
                                 Permanent permanent, Card preCopyCard, Duration duration) {
        gameData.addEffectOnPermanent(sourceCardName, sourcePermanentId, controllerId,
                new ResolvedCopyEffect(permanent.getCard(), preCopyCard), permanent.getId(), duration);
    }

    /**
     * Has the active copy effects of {@code permanent} from resolutions keep {@code copiedCard} and {@code preCopyCard}
     * instead: a newer copy effect of another kind, or a merge, changed what they apply and what the permanent reverts
     * to.
     */
    public static void updateCopies(GameData gameData, Permanent permanent,
                                    java.util.function.UnaryOperator<Card> copiedCard,
                                    java.util.function.UnaryOperator<Card> preCopyCard) {
        for (FloatingContinuousEffect copy : resolvedCopies(gameData, permanent.getId())) {
            var kept = (ResolvedCopyEffect) copy.effect();
            gameData.replaceFloatingEffect(copy, copy.withEffect(new ResolvedCopyEffect(
                    copiedCard.apply(kept.copiedCard()), preCopyCard.apply(kept.preCopyCard()))));
        }
    }

    /** The active copy effects from resolutions on {@code permanentId}, oldest first. */
    public static List<FloatingContinuousEffect> resolvedCopies(GameData gameData, UUID permanentId) {
        return gameData.floatingEffectsOnPermanent(permanentId, ResolvedCopyEffect.class).stream()
                .sorted(java.util.Comparator.comparingLong(FloatingContinuousEffect::timestamp))
                .toList();
    }

    /**
     * What {@code affected} shows once its copy effects from resolutions in {@code ended} have ended together: when the
     * newest of them was newer than every copy effect left, the newest one left applies again (CR 613.7), or, with none
     * left, the card it showed before them. A copy effect ending under a newer one leaves the newer one in place.
     */
    public static void endCopies(GameData gameData, Permanent affected, List<FloatingContinuousEffect> ended) {
        FloatingContinuousEffect newestEnded = ended.stream()
                .max(java.util.Comparator.comparingLong(FloatingContinuousEffect::timestamp))
                .orElse(null);
        if (newestEnded == null) {
            return;
        }
        List<FloatingContinuousEffect> remaining = resolvedCopies(gameData, affected.getId());
        if (remaining.isEmpty()) {
            affected.setCard(((ResolvedCopyEffect) newestEnded.effect()).preCopyCard());
            return;
        }
        FloatingContinuousEffect newest = remaining.getLast();
        if (newest.timestamp() < newestEnded.timestamp()) {
            affected.setCard(((ResolvedCopyEffect) newest.effect()).copiedCard());
        }
    }
}
