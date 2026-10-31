package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PrimalClayForm;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseIndependentModesOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentEntryCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.LicidEndEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class PermanentCopierService {

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride, Integer toughnessOverride) {
        applyCloneCopy(clonePerm, targetPerm, powerOverride, toughnessOverride, Set.of());
    }

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride,
                                Integer toughnessOverride, Set<CardType> additionalTypesOverride) {
        applyCloneCopy(clonePerm, copiableCard(targetPerm), powerOverride, toughnessOverride, additionalTypesOverride);
    }

    /** Includes characteristics selected by independent as-enters choices in a copy's base. */
    public Card copiableCard(Permanent permanent) {
        Card copiable = printedCopiableCard(permanent);
        PrimalClayForm shape = permanent.getPrimalClayForm();
        if (permanent.isFaceDown()
                || permanent.getCopiableEntryCharacteristics().isEmpty() && shape == null) {
            return copiable;
        }
        // "As ... enters" conversions that set power and toughness are copiable values (CR 707.2).
        Card converted = copiable.createRuntimeCopy();
        Set<CardType> additionalTypes = EnumSet.noneOf(CardType.class);
        additionalTypes.addAll(converted.getAdditionalTypes());
        List<CardSubtype> subtypes = new ArrayList<>(converted.getSubtypes());
        for (ControlledPermanentEntryCharacteristicsEffect characteristics : permanent.getCopiableEntryCharacteristics()) {
            characteristics.addedCardTypes().stream()
                    .filter(type -> type != converted.getType())
                    .forEach(additionalTypes::add);
            characteristics.addedSubtypes().stream()
                    .filter(subtype -> !subtypes.contains(subtype))
                    .forEach(subtypes::add);
            if (characteristics.basePower() != null) {
                converted.setPower(characteristics.basePower());
            }
            if (characteristics.baseToughness() != null) {
                converted.setToughness(characteristics.baseToughness());
            }
        }
        if (shape != null) {
            // Primal Clay's shape is an "as enters" conversion too: a copy takes its keyword and Wall type, while
            // its P/T comes from the copy's own shape choice, which overrides this base (Gatherer ruling, Primal Clay).
            converted.setPower(shape.power());
            converted.setToughness(shape.toughness());
            if (shape.keyword() != null) {
                Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
                keywords.addAll(converted.getKeywords());
                keywords.add(shape.keyword());
                converted.setKeywords(keywords);
            }
            if (shape.subtype() != null && !subtypes.contains(shape.subtype())) {
                subtypes.add(shape.subtype());
            }
        }
        converted.setAdditionalTypes(additionalTypes);
        converted.setSubtypes(subtypes);
        return converted;
    }

    private Card printedCopiableCard(Permanent permanent) {
        if (permanent.isFaceDown()) {
            Card visible = new Card();
            visible.setName("");
            visible.setType(CardType.CREATURE);
            visible.setManaCost("");
            visible.setPower(permanent.getBasePower());
            visible.setToughness(permanent.getBaseToughness());
            return visible;
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
        if (!permanent.getNonCopiableKeywords().isEmpty()) {
            Card withoutGrantedKeywords = original.createRuntimeCopy();
            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(original.getKeywords());
            keywords.removeAll(permanent.getNonCopiableKeywords());
            withoutGrantedKeywords.setKeywords(keywords);
            original = withoutGrantedKeywords;
        }
        if (original.getActivatedAbilities().stream().anyMatch(ability -> ability.getEffects().stream()
                .anyMatch(LicidEndEffect.class::isInstance))) {
            // A Licid that became an Aura: that is the effect of its ability, not a copiable value, so a
            // copy sees the printed creature form (CR 707.2).
            return permanent.getOriginalCard();
        }
        FlipCoinOnEnterEffect entryFlip = permanent.getEntryCoinFlipHeads() == null ? null
                : original.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .filter(FlipCoinOnEnterEffect.class::isInstance)
                        .map(FlipCoinOnEnterEffect.class::cast)
                        .findFirst().orElse(null);
        if (entryFlip != null) {
            // A permanent that copies this one already on the battlefield takes the form the entry
            // coin flip fixed, without flipping again (Molten Sentry ruling).
            boolean heads = permanent.getEntryCoinFlipHeads();
            Card formCopy = original.createRuntimeCopy();
            formCopy.setPower(heads ? entryFlip.headsPower() : entryFlip.tailsPower());
            formCopy.setToughness(heads ? entryFlip.headsToughness() : entryFlip.tailsToughness());
            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(formCopy.getKeywords());
            keywords.addAll(heads ? entryFlip.headsKeywords() : entryFlip.tailsKeywords());
            formCopy.setKeywords(keywords);
            return formCopy;
        }
        if (original.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .noneMatch(ChooseIndependentModesOnEnterEffect.class::isInstance)) {
            return original;
        }
        Card copy = original.createRuntimeCopy();
        for (EffectSlot slot : EffectSlot.values()) {
            List<EffectRegistration> registrations = copy.getEffectRegistrations(slot);
            if (registrations.isEmpty()) continue;
            List<EffectRegistration> selectedEffects = new ArrayList<>();
            for (EffectRegistration registration : registrations) {
                if (registration.effect() instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof SourceHasChosenMode chosenMode
                        && permanent.getChosenModeLabels().contains(chosenMode.mode())) {
                    if (conditional.wrapped() instanceof SetBasePowerToughnessEffect set
                            && set.scope() == GrantScope.SELF) {
                        copy.setPower(set.power());
                        copy.setToughness(set.toughness());
                        continue;
                    }
                    selectedEffects.add(new EffectRegistration(conditional.wrapped(), registration.triggerMode(),
                            registration.simultaneousWithPrevious()));
                }
            }
            registrations.addAll(0, selectedEffects);
        }
        return copy;
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
        for (EffectSlot slot : EffectSlot.values()) {
            for (EffectRegistration reg : target.getEffectRegistrations(slot)) {
                // CR 707.9d: when a copy effect provides specific P/T values,
                // characteristic-defining abilities that define P/T are not copied
                if (hasPTOverride && reg.effect().isPowerToughnessDefining()) {
                    continue;
                }
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }
        for (ActivatedAbility ability : target.getActivatedAbilities()) {
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
}
