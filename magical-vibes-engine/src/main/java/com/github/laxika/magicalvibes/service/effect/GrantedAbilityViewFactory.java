package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingSourceKind;
import com.github.laxika.magicalvibes.networking.model.GrantedAbilityView;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Projects final engine ability state into stable, human-readable wire views. Raw
 * {@link CardEffect} implementations deliberately stay inside the engine.
 */
@Component
public class GrantedAbilityViewFactory {

    public List<GrantedAbilityView> create(
            Permanent permanent,
            GameQueryService.StaticBonus bonus,
            List<GrantedEffectAttribution> attributions) {
        List<GrantedAbilityView> result = new ArrayList<>();
        Set<CardColor> protectionCoveredByAttributedEffects = EnumSet.noneOf(CardColor.class);

        Map<CardEffect, List<String>> sourcesByEffect = new IdentityHashMap<>();
        for (GrantedEffectAttribution attribution : attributions) {
            sourcesByEffect.computeIfAbsent(attribution.effect(), ignored -> new ArrayList<>())
                    .add(attribution.sourceName());
        }

        Set<CardEffect> finalGrantedEffects =
                Collections.newSetFromMap(new IdentityHashMap<>());
        finalGrantedEffects.addAll(bonus.grantedEffects());
        for (CardEffect effect : bonus.grantedEffects()) {
            String text = format(effect);
            if (text == null) {
                continue;
            }
            if (effect instanceof ProtectionGrantingEffect protection) {
                protectionCoveredByAttributedEffects.addAll(protection.protectionFromColors());
            }
            List<String> sources = sourcesByEffect.get(effect);
            if (sources == null || sources.isEmpty()) {
                result.add(new GrantedAbilityView(text, null));
            } else {
                for (String source : sources) {
                    result.add(new GrantedAbilityView(text, source));
                }
            }
        }

        for (GrantedEffectAttribution attribution : attributions) {
            if (finalGrantedEffects.contains(attribution.effect())) {
                continue;
            }
            String text = format(attribution.effect());
            if (text == null) {
                continue;
            }
            if (attribution.effect() instanceof ProtectionGrantingEffect protection) {
                protectionCoveredByAttributedEffects.addAll(protection.protectionFromColors());
            }
            result.add(new GrantedAbilityView(text, attribution.sourceName()));
        }

        Set<CardColor> printedProtection = printedProtectionColors(permanent);
        Set<CardColor> remainingProtection = EnumSet.noneOf(CardColor.class);
        remainingProtection.addAll(bonus.protectionColors());
        remainingProtection.addAll(permanent.getProtectionFromColorsUntilEndOfTurn());
        remainingProtection.removeAll(printedProtection);
        remainingProtection.removeAll(protectionCoveredByAttributedEffects);

        CardColor chosenProtection = chosenProtectionColor(permanent);
        if (chosenProtection != null && remainingProtection.remove(chosenProtection)) {
            result.add(new GrantedAbilityView(
                    formatProtectionColors(Set.of(chosenProtection)),
                    permanent.getCard().getName()));
        }
        if (!remainingProtection.isEmpty()) {
            result.add(new GrantedAbilityView(formatProtectionColors(remainingProtection), null));
        }
        if (permanent.isCantBeBlocked()) {
            result.add(new GrantedAbilityView("Can't be blocked", null));
        }
        for (CardSubtype subtype : permanent.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()
                .stream().sorted(Comparator.comparingInt(CardSubtype::ordinal)).toList()) {
            result.add(new GrantedAbilityView(
                    "Protection from non-" + formatSubtype(subtype) + " creatures", null));
        }

        // Multiple sources may grant the same final ability. Keep their separate source rows,
        // but collapse exact duplicates caused by repeated query paths.
        Map<String, GrantedAbilityView> distinct = new LinkedHashMap<>();
        for (GrantedAbilityView view : result) {
            distinct.putIfAbsent(view.text() + "\u0000" + view.sourceName(), view);
        }
        return List.copyOf(distinct.values());
    }

    private Set<CardColor> printedProtectionColors(Permanent permanent) {
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionFromColorsEffect protection && protection.scope() == null) {
                ProtectionFromColorsEffect rewritten = (ProtectionFromColorsEffect)
                        TextChangeTransformer.transform(protection, permanent.getTextReplacements());
                colors.addAll(rewritten.colors());
            }
        }
        return colors;
    }

    private CardColor chosenProtectionColor(Permanent permanent) {
        if (permanent.getChosenColor() == null) {
            return null;
        }
        boolean grantsChosenProtection = permanent.getCard()
                .getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(ProtectionFromChosenColorEffect.class::isInstance);
        return grantsChosenProtection ? permanent.getChosenColor() : null;
    }

    String format(CardEffect effect) {
        if (effect instanceof ProtectionGrantingEffect protection) {
            return formatProtection(protection);
        }
        return switch (effect) {
            case CantBeBlockedEffect ignored -> "Can't be blocked";
            case TargetingRestrictionEffect restriction -> formatTargetingRestriction(restriction);
            case CantBeEnchantedByOtherAurasEffect ignored -> "Can't be enchanted by other Auras";
            case CantHaveMinusOneMinusOneCountersEffect ignored ->
                    "Can't have \u22121/\u22121 counters put on it";
            default -> null;
        };
    }

    private String formatProtection(ProtectionGrantingEffect protection) {
        if (protection.protectsFromEverything()) {
            return "Protection from everything";
        }
        if (!protection.protectionFromColors().isEmpty()) {
            return formatProtectionColors(protection.protectionFromColors());
        }
        if (!protection.protectionFromCardTypes().isEmpty()) {
            return "Protection from " + enumPhrase(protection.protectionFromCardTypes());
        }
        if (!protection.protectionFromSubtypes().isEmpty()) {
            return "Protection from " + enumPhrase(protection.protectionFromSubtypes());
        }
        if (protection.protectionFromManaValueAtLeast().isPresent()) {
            return "Protection from mana value "
                    + protection.protectionFromManaValueAtLeast().getAsInt() + " or greater";
        }
        return null;
    }

    private String formatProtectionColors(Set<CardColor> colors) {
        if (colors.size() == CardColor.values().length) {
            return "Protection from each color";
        }
        return "Protection from " + enumPhrase(colors);
    }

    private String formatTargetingRestriction(TargetingRestrictionEffect restriction) {
        if (restriction.mode() == TargetColorMode.ANY) {
            if (restriction.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                    && restriction.opponentOnly()) {
                return "Hexproof";
            }
            if (restriction.kind() == TargetingSourceKind.SPELLS) {
                return "Can't be the target of spells";
            }
            if (restriction.kind() == TargetingSourceKind.ABILITIES && restriction.opponentOnly()) {
                return "Abilities your opponents control can't target this permanent";
            }
            return "Can't be targeted by spells or abilities";
        }
        String colors = enumPhrase(restriction.colors());
        if (restriction.mode() == TargetColorMode.BLOCKED_COLORS) {
            if (restriction.opponentOnly()) {
                return "Hexproof from " + colors;
            }
            return "Can't be the target of " + colors + " "
                    + (restriction.kind() == TargetingSourceKind.SPELLS
                    ? "spells" : "spells or abilities");
        }
        return "Can't be the target of spells or abilities from non-" + colors + " sources";
    }

    private String enumPhrase(Iterable<? extends Enum<?>> values) {
        List<Enum<?>> ordered = new ArrayList<>();
        for (Enum<?> value : values) {
            ordered.add(value);
        }
        ordered.sort(Comparator.comparingInt(value -> value.ordinal()));
        List<String> words = ordered.stream()
                .map(value -> value.name().toLowerCase(Locale.ROOT).replace('_', ' '))
                .toList();
        if (words.size() <= 1) {
            return words.isEmpty() ? "" : words.getFirst();
        }
        if (words.size() == 2) {
            return words.get(0) + " and " + words.get(1);
        }
        return String.join(", ", words.subList(0, words.size() - 1))
                + ", and " + words.getLast();
    }

    private String formatSubtype(CardSubtype subtype) {
        String[] words = subtype.name().toLowerCase(Locale.ROOT).split("_");
        for (int i = 0; i < words.length; i++) {
            words[i] = Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
        }
        return String.join(" ", words);
    }
}
