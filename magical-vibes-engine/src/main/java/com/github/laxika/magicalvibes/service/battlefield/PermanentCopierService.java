package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseIndependentModesOnEnterEffect;
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
        if (permanent.isFaceDown()) {
            Card visible = new Card();
            visible.setName("");
            visible.setType(CardType.CREATURE);
            visible.setManaCost("");
            visible.setPower(permanent.getBasePower());
            visible.setToughness(permanent.getBaseToughness());
            return visible;
        }
        Card original = permanent.getCard();
        if (original.getActivatedAbilities().stream().anyMatch(ability -> ability.getEffects().stream()
                .anyMatch(LicidEndEffect.class::isInstance))) {
            // A Licid that became an Aura: that is the effect of its ability, not a copiable value, so a
            // copy sees the printed creature form (CR 707.2).
            return permanent.getOriginalCard();
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
                    selectedEffects.add(new EffectRegistration(conditional.wrapped(), registration.triggerMode()));
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
        copy.setColor(copyColor ? target.getColor() : null);
        copy.setColors(copyColor ? target.getColors() : List.of());
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
