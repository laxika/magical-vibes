package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEquipByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToCreaturesOfChosenParityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.layer.Layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static registry mapping every continuous-effect type the engine knows to the CR 613 layer(s)
 * it contributes to. Classification only — nothing consumes it yet; the layered engine (the
 * next CR 613 migration step) will. Per-card reasoning for the non-obvious mappings lives in
 * {@code agent-docs/LAYER_SYSTEM.md} under "Classification notes".
 *
 * <p>One effect may contribute to several layers with a single timestamp: an anthem that also
 * grants keywords ({@link StaticBoostEffect}) contributes to layer 6 and sublayer 7c; an
 * animate-and-set-P/T effect ({@link AnimateNoncreatureArtifactsEffect}) contributes to layer 4
 * and sublayer 7b (CR 613.4).
 *
 * <p>New static effects MUST be registered here — {@code LayerClassifierTest} walks every
 * handler in {@link StaticEffectHandlerRegistry} and fails on unclassified effect types.
 */
public final class LayerClassifier {

    /**
     * The layer contribution of one continuous effect.
     *
     * @param layers                 the layers the effect contributes to (never empty)
     * @param characteristicDefining applied before the timestamp-ordered effects of its layer
     *                               (CR 613.2b for layers 1-6, CR 613.4a for sublayer 7a)
     * @param colorSetting           meaningful only for {@link Layer#L5_COLOR} contributions:
     *                               {@code true} if the effect replaces the object's colors
     *                               ("becomes red", CR 105.3), {@code false} if it adds them
     *                               ("in addition to its other colors")
     */
    public record LayerClassification(Set<Layer> layers, boolean characteristicDefining, boolean colorSetting) {
    }

    @FunctionalInterface
    private interface Rule {

        /**
         * @param fromOwnStaticSlot {@code true} when the effect affects its own source
         *                          (source == affected) and comes from the object's own
         *                          {@code EffectSlot.STATIC} — the CDA criterion (CR 604.3)
         */
        LayerClassification classify(CardEffect effect, boolean fromOwnStaticSlot);
    }

    private record Entry(Set<Layer> possibleLayers, Rule rule) {
    }

    private static final Map<Class<? extends CardEffect>, Entry> ENTRIES = buildEntries();

    private LayerClassifier() {
    }

    /**
     * Classifies one continuous effect into the layer(s) it contributes to.
     *
     * @param fromOwnStaticSlot {@code true} when the effect affects its own source and comes
     *                          from that object's own {@code EffectSlot.STATIC} (see {@link Rule})
     * @throws IllegalArgumentException for effect types with no registered classification
     */
    public static LayerClassification classify(CardEffect effect, boolean fromOwnStaticSlot) {
        Entry entry = ENTRIES.get(effect.getClass());
        if (entry == null) {
            throw new IllegalArgumentException("No layer classification for effect type "
                    + effect.getClass().getSimpleName());
        }
        return entry.rule().classify(effect, fromOwnStaticSlot);
    }

    /**
     * Every layer the effect type can contribute to, unioned over all instances and contexts
     * (e.g. {@link SetPowerToughnessToAmountEffect} yields 7a and 7b). Empty for unclassified
     * types — the coverage test asserts non-empty for every registered static handler.
     */
    public static Set<Layer> possibleLayers(Class<? extends CardEffect> effectType) {
        Entry entry = ENTRIES.get(effectType);
        return entry == null ? Set.of() : entry.possibleLayers();
    }

    private static Entry fixed(Layer... layers) {
        Set<Layer> layerSet = Set.of(layers);
        LayerClassification classification = new LayerClassification(layerSet, false, false);
        return new Entry(layerSet, (effect, fromOwnStaticSlot) -> classification);
    }

    private static Entry fixedCharacteristicDefining(Layer... layers) {
        Set<Layer> layerSet = Set.of(layers);
        LayerClassification classification = new LayerClassification(layerSet, true, false);
        return new Entry(layerSet, (effect, fromOwnStaticSlot) -> classification);
    }

    private static Map<Class<? extends CardEffect>, Entry> buildEntries() {
        Map<Class<? extends CardEffect>, Entry> map = new LinkedHashMap<>();

        // Layer 1 — copy effects (CR 613.2a).
        map.put(BecomeCopyOfTargetCreatureEffect.class, fixed(Layer.L1_COPY));
        map.put(BecomeCopyOfTargetCreatureUntilEndOfTurnEffect.class, fixed(Layer.L1_COPY));
        map.put(CopyPermanentOnEnterEffect.class, fixed(Layer.L1_COPY));

        // Layer 2 — control-changing effects (CR 613.2b).
        map.put(GainControlOfTargetEffect.class, fixed(Layer.L2_CONTROL));
        map.put(GainControlOfEnchantedTargetEffect.class, fixed(Layer.L2_CONTROL));
        map.put(GainControlOfTargetAuraEffect.class, fixed(Layer.L2_CONTROL));
        map.put(ControlEnchantedCreatureEffect.class, fixed(Layer.L2_CONTROL));
        map.put(TargetPlayerGainsControlOfSourceCreatureEffect.class, fixed(Layer.L2_CONTROL));

        // Layer 3 — text-changing effects (CR 613.2c / CR 612).
        map.put(ChangeColorTextEffect.class, fixed(Layer.L3_TEXT));

        // Layer 4 — type-changing effects (card types, subtypes, supertypes).
        map.put(GrantSubtypeEffect.class, fixed(Layer.L4_TYPE));
        map.put(GrantCardTypeEffect.class, fixed(Layer.L4_TYPE));
        map.put(GrantSupertypeToEnchantedPermanentEffect.class, fixed(Layer.L4_TYPE));
        map.put(GrantChosenSubtypeToOwnCreaturesEffect.class, fixed(Layer.L4_TYPE));
        map.put(EnchantedPermanentBecomesTypeEffect.class, fixed(Layer.L4_TYPE));
        map.put(EnchantedPermanentBecomesChosenTypeEffect.class, fixed(Layer.L4_TYPE));
        map.put(NonbasicLandsBecomeTypeEffect.class, fixed(Layer.L4_TYPE));
        map.put(LoseAllCreatureTypesEffect.class, fixed(Layer.L4_TYPE));
        // Animate-and-set-P/T: the type change applies in layer 4, the MV-based base P/T in
        // sublayer 7b, both with ONE timestamp (CR 613.4, March of the Machines).
        map.put(AnimateNoncreatureArtifactsEffect.class, fixed(Layer.L4_TYPE, Layer.L7B_SET_PT));

        // Layer 5 — color-changing effects. colorSetting distinguishes "becomes [color]"
        // (replaces, CR 105.3) from "in addition to its other colors" (adds).
        map.put(GrantColorEffect.class, new Entry(Set.of(Layer.L5_COLOR), (effect, fromOwnStaticSlot) ->
                new LayerClassification(Set.of(Layer.L5_COLOR), false, ((GrantColorEffect) effect).overriding())));
        // Both users ("becomes red" — Incite, "becomes blue" — Grand Architect) are setting.
        map.put(GrantColorUntilEndOfTurnEffect.class, new Entry(Set.of(Layer.L5_COLOR), (effect, fromOwnStaticSlot) ->
                new LayerClassification(Set.of(Layer.L5_COLOR), false, true)));

        // Layer 6 — ability adding/removing.
        map.put(GrantKeywordEffect.class, fixed(Layer.L6_ABILITIES));
        map.put(RemoveKeywordEffect.class, fixed(Layer.L6_ABILITIES));
        map.put(LosesAllAbilitiesEffect.class, fixed(Layer.L6_ABILITIES));
        map.put(GrantActivatedAbilityEffect.class, fixed(Layer.L6_ABILITIES));
        map.put(GrantEffectEffect.class, fixed(Layer.L6_ABILITIES));
        map.put(GrantEquipByManaValueEffect.class, fixed(Layer.L6_ABILITIES));
        map.put(ProtectionFromColorsEffect.class, fixed(Layer.L6_ABILITIES));
        map.put(ProtectionFromChosenColorEffect.class, fixed(Layer.L6_ABILITIES));
        map.put(GrantKeywordToCreaturesOfChosenParityEffect.class, fixed(Layer.L6_ABILITIES));
        // Self ability-scans (Cairn Wanderer family) carry the CDA flag as a modeling decision
        // of the migration plan — see LAYER_SYSTEM.md "Classification notes".
        map.put(GainKeywordsOfCreatureCardsInAllGraveyardsEffect.class,
                fixedCharacteristicDefining(Layer.L6_ABILITIES));
        map.put(GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect.class,
                fixedCharacteristicDefining(Layer.L6_ABILITIES));
        map.put(GainActivatedAbilitiesOfExiledCardsEffect.class,
                fixedCharacteristicDefining(Layer.L6_ABILITIES));

        // Sublayers 7a/7b — P/T setting. A "P/T equal to [amount]" static in the creature's
        // own STATIC slot is the characteristic-defining */* ability (7a, CR 613.4a); the same
        // effect applied to something else (or by a resolved spell) sets base P/T in 7b.
        map.put(SetPowerToughnessToAmountEffect.class,
                new Entry(Set.of(Layer.L7A_CDA, Layer.L7B_SET_PT), (effect, fromOwnStaticSlot) ->
                        fromOwnStaticSlot
                                ? new LayerClassification(Set.of(Layer.L7A_CDA), true, false)
                                : new LayerClassification(Set.of(Layer.L7B_SET_PT), false, false)));
        map.put(SetBasePowerToughnessEffect.class, fixed(Layer.L7B_SET_PT));

        // Sublayer 7c — P/T additions.
        map.put(StaticBoostEffect.class, new Entry(Set.of(Layer.L6_ABILITIES, Layer.L7C_MODIFY_PT),
                (effect, fromOwnStaticSlot) -> {
                    StaticBoostEffect boost = (StaticBoostEffect) effect;
                    boolean grantsKeywords = boost.grantedKeywords() != null && !boost.grantedKeywords().isEmpty();
                    return new LayerClassification(
                            grantsKeywords
                                    ? Set.of(Layer.L6_ABILITIES, Layer.L7C_MODIFY_PT)
                                    : Set.of(Layer.L7C_MODIFY_PT),
                            false, false);
                }));
        map.put(BoostSelfEffect.class, fixed(Layer.L7C_MODIFY_PT));
        map.put(AttachedBoostEffect.class, fixed(Layer.L7C_MODIFY_PT));
        map.put(BoostTargetCreatureEffect.class, fixed(Layer.L7C_MODIFY_PT));
        map.put(BoostByOtherCreaturesWithSameNameEffect.class, fixed(Layer.L7C_MODIFY_PT));
        map.put(BoostBySharedCreatureTypeEffect.class, fixed(Layer.L7C_MODIFY_PT));
        map.put(BoostCreaturesOfChosenColorEffect.class, fixed(Layer.L7C_MODIFY_PT));
        map.put(BoostCreaturesOfChosenSubtypeEffect.class, fixed(Layer.L7C_MODIFY_PT));

        // Sublayer 7d — P/T switching.
        map.put(SwitchPowerToughnessEffect.class, fixed(Layer.L7D_SWITCH_PT));

        // Wrappers classify by what they wrap; the declared union spans every layer a wrapped
        // static effect can contribute to (conditions never wrap copy/control/text effects).
        Set<Layer> anyWrappedLayer = Collections.unmodifiableSet(
                EnumSet.range(Layer.L4_TYPE, Layer.L7D_SWITCH_PT));
        map.put(ConditionalEffect.class, new Entry(anyWrappedLayer, (effect, fromOwnStaticSlot) ->
                classify(((ConditionalEffect) effect).wrapped(), fromOwnStaticSlot)));
        map.put(EnchantedPermanentConditionalEffect.class, new Entry(anyWrappedLayer,
                (effect, fromOwnStaticSlot) -> {
                    EnchantedPermanentConditionalEffect conditional = (EnchantedPermanentConditionalEffect) effect;
                    List<CardEffect> branches = new ArrayList<>();
                    if (conditional.ifMatch() != null) {
                        branches.add(conditional.ifMatch());
                    }
                    if (conditional.ifNotMatch() != null) {
                        branches.add(conditional.ifNotMatch());
                    }
                    EnumSet<Layer> layers = EnumSet.noneOf(Layer.class);
                    boolean characteristicDefining = false;
                    boolean colorSetting = false;
                    for (CardEffect branch : branches) {
                        LayerClassification classification = classify(branch, fromOwnStaticSlot);
                        layers.addAll(classification.layers());
                        characteristicDefining |= classification.characteristicDefining();
                        colorSetting |= classification.colorSetting();
                    }
                    return new LayerClassification(Set.copyOf(layers), characteristicDefining, colorSetting);
                }));

        return Map.copyOf(map);
    }
}
