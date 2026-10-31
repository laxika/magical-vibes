package com.github.laxika.magicalvibes.service.effect.replacement.mana;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BasicLandManaProducesAnyColorEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LandManaProducesFixedColorEffect;
import com.github.laxika.magicalvibes.model.effect.ManaReflectionEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.TwistBasicLandManaColorsEffect;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Land-mana replacements printed on permanents, one option per effect instance:
 * <ul>
 *   <li>{@link LandManaProducesFixedColorEffect} — Infernal Darkness makes a land produce one type
 *       instead of any other; Contamination also sets the amount;</li>
 *   <li>{@link BasicLandManaProducesAnyColorEffect} — Pulse of Llanowar lets its controller's
 *       basic lands produce a color of their choice;</li>
 *   <li>{@link TwistBasicLandManaColorsEffect} — Reality Twist remaps basic land types to colors;</li>
 *   <li>{@link ReplaceLandExcessManaWithColorlessEffect} — Damping Sphere turns two or more mana
 *       from a land into {C};</li>
 *   <li>{@link ManaReflectionEffect} — Mana Reflection multiplies the mana its controller taps a
 *       matching permanent for.</li>
 * </ul>
 * Effects on permanents that have lost all abilities do nothing.
 */
@Component
class StaticLandManaReplacements extends LandManaCandidateSource {

    private static final String FIXED = "LandManaFixedColor:";
    private static final String ANY_COLOR = "BasicLandManaAnyColor:";
    private static final String TWIST = "TwistLandManaColors:";
    private static final String DAMPING = "LandExcessManaColorless:";
    private static final String MULTIPLIER = "ManaProductionMultiplier:";

    private final PredicateEvaluationService predicateEvaluationService;

    StaticLandManaReplacements(GameQueryService gameQueryService,
                               PredicateEvaluationService predicateEvaluationService) {
        super(gameQueryService);
        this.predicateEvaluationService = predicateEvaluationService;
    }

    private record SourcedEffect(Permanent source, UUID controllerId, int index, CardEffect effect) {

        String key(String family) {
            return family + source.getId() + ":" + index;
        }

        String name() {
            return source.getCard().getName();
        }
    }

    @Override
    protected List<ReplacementOption> applicable(GameData gameData, LandManaEvent event, Permanent land) {
        UUID landControllerId = gameQueryService.findPermanentController(gameData, land.getId());
        boolean basicLand = gameQueryService.hasEffectiveSupertype(gameData, land, CardSupertype.BASIC);
        Set<CardSubtype> landTypes = gameQueryService.effectiveBasicLandTypes(gameData, land);
        List<ReplacementOption> options = new ArrayList<>();
        for (SourcedEffect sourced : activeEffects(gameData)) {
            CardEffect effect = sourced.effect();
            if (effect instanceof LandManaProducesFixedColorEffect fixed) {
                options.add(option(sourced.key(FIXED), sourced.name() + ": produces " + manaText(fixed.color())
                        + (fixed.fixedAmount() == null ? "" : " (amount " + fixed.fixedAmount() + ")")));
            } else if (effect instanceof BasicLandManaProducesAnyColorEffect && basicLand
                    && sourced.controllerId().equals(landControllerId)) {
                options.add(option(sourced.key(ANY_COLOR), sourced.name() + ": produces a color of your choice"));
            } else if (effect instanceof TwistBasicLandManaColorsEffect twist
                    && landTypes.stream().anyMatch(twist.landColorMapping()::containsKey)) {
                options.add(option(sourced.key(TWIST), sourced.name() + ": produces "
                        + String.join(" or ", twistedColors(twist, landTypes).stream()
                                .map(LandManaCandidateSource::manaText).toList())));
            } else if (effect instanceof ReplaceLandExcessManaWithColorlessEffect && event.amount() >= 2) {
                options.add(option(sourced.key(DAMPING), sourced.name() + ": produces {C} instead"));
            } else if (effect instanceof ManaReflectionEffect reflection
                    && sourced.controllerId().equals(event.playerId())
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, land, reflection.permanentFilter())) {
                options.add(option(sourced.key(MULTIPLIER), sourced.name() + ": produces "
                        + MaroGoneNutsSupport.apply(gameData, reflection, reflection.multiplier()) + " times as much"));
            }
        }
        return options;
    }

    @Override
    public LandManaEvent transform(GameData gameData, LandManaEvent event, ReplacementOption option) {
        SourcedEffect sourced = activeEffects(gameData).stream()
                .filter(candidate -> option.key().endsWith(candidate.source().getId() + ":" + candidate.index()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Mana replacement source left: " + option.key()));
        CardEffect effect = sourced.effect();
        if (effect instanceof LandManaProducesFixedColorEffect fixed) {
            LandManaEvent recolored = event.withColors(Set.of(fixed.color()));
            return fixed.fixedAmount() == null ? recolored : recolored.withAmount(fixed.fixedAmount());
        }
        if (effect instanceof BasicLandManaProducesAnyColorEffect) {
            return event.withColors(anyColor());
        }
        if (effect instanceof TwistBasicLandManaColorsEffect twist) {
            Permanent land = gameQueryService.findPermanentById(gameData, event.permanentId());
            return event.withColors(twistedColors(twist, gameQueryService.effectiveBasicLandTypes(gameData, land)));
        }
        if (effect instanceof ReplaceLandExcessManaWithColorlessEffect) {
            return event.withColors(Set.of(ManaColor.COLORLESS)).withAmount(1);
        }
        ManaReflectionEffect reflection = (ManaReflectionEffect) effect;
        return event.withAmount(event.amount() * MaroGoneNutsSupport.apply(gameData, reflection, reflection.multiplier()));
    }

    private List<SourcedEffect> activeEffects(GameData gameData) {
        List<SourcedEffect> effects = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent source : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (gameQueryService.hasLostAllAbilities(gameData, source)) {
                    continue;
                }
                List<CardEffect> statics = source.getCard().getStaticEffects();
                for (int i = 0; i < statics.size(); i++) {
                    CardEffect effect = statics.get(i);
                    if (effect instanceof LandManaProducesFixedColorEffect
                            || effect instanceof BasicLandManaProducesAnyColorEffect
                            || effect instanceof TwistBasicLandManaColorsEffect
                            || effect instanceof ReplaceLandExcessManaWithColorlessEffect
                            || effect instanceof ManaReflectionEffect) {
                        effects.add(new SourcedEffect(source, playerId, i, effect));
                    }
                }
            }
        }
        return effects;
    }

    /** Each of the land's basic types as this effect remaps it; unmapped types keep their color. */
    private static Set<ManaColor> twistedColors(TwistBasicLandManaColorsEffect twist, Set<CardSubtype> landTypes) {
        Set<ManaColor> colors = EnumSet.noneOf(ManaColor.class);
        for (CardSubtype type : landTypes) {
            ManaColor color = twist.landColorMapping().getOrDefault(type, intrinsicColor(type));
            if (color != null) {
                colors.add(color);
            }
        }
        return colors;
    }

    private static ManaColor intrinsicColor(CardSubtype type) {
        return switch (type) {
            case PLAINS -> ManaColor.WHITE;
            case ISLAND -> ManaColor.BLUE;
            case SWAMP -> ManaColor.BLACK;
            case MOUNTAIN -> ManaColor.RED;
            case FOREST -> ManaColor.GREEN;
            default -> null;
        };
    }
}
