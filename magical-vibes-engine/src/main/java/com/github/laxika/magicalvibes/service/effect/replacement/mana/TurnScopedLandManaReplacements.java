package com.github.laxika.magicalvibes.service.effect.replacement.mana;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceTargetLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Land-mana replacements that resolving spells and abilities create, recorded on {@link GameData}:
 * <ul>
 *   <li>Deep Water — lands a player controls produce one fixed color
 *       ({@code landManaFixedColorThisTurn});</li>
 *   <li>Harvest Mage — lands a player taps produce one mana of a color of their choice instead of
 *       any other type and amount ({@code playersWithLandManaChoiceReplacementThisTurn});</li>
 *   <li>Pale Moon — nonbasic lands produce a fixed type ({@code nonbasicLandsFixedManaColorThisTurn});</li>
 *   <li>Chaos Moon — lands of a basic land type produce a fixed type
 *       ({@code landSubtypeFixedManaColorThisTurn});</li>
 *   <li>Hall of Gemstone — lands produce the chosen color instead of any other <em>color</em>, so
 *       it leaves colorless mana alone ({@code allLandsFixedManaColorThisTurn});</li>
 *   <li>False Dawn — abilities a player controls that would add colored mana add that much white
 *       mana instead ({@code playersWithColoredManaReplacementThisTurn});</li>
 *   <li>Quarum Trench Gnomes — one land produces another type instead of one type, indefinitely
 *       (a floating {@link ReplaceTargetLandManaWithColorEffect}).</li>
 * </ul>
 */
@Component
class TurnScopedLandManaReplacements extends LandManaCandidateSource {

    private static final String CONTROLLER_FIXED = "ControllerLandsFixedColor:";
    private static final String CONTROLLER_CHOICE = "ControllerLandsChosenColor:";
    private static final String NONBASIC_FIXED = "NonbasicLandsFixedColor";
    private static final String SUBTYPE_FIXED = "LandSubtypeFixedColor:";
    private static final String ALL_LANDS_COLOR = "AllLandsChosenColor";
    private static final String COLORED_TO_WHITE = "ColoredManaBecomesWhite:";
    private static final String TARGET_LAND_COLOR = "TargetLandManaColor:";

    TurnScopedLandManaReplacements(GameQueryService gameQueryService) {
        super(gameQueryService);
    }

    @Override
    protected List<ReplacementOption> applicable(GameData gameData, LandManaEvent event, Permanent land) {
        List<ReplacementOption> options = new ArrayList<>();
        UUID controllerId = gameQueryService.findPermanentController(gameData, land.getId());
        boolean tappedByController = controllerId != null && controllerId.equals(event.playerId());
        ManaColor controllerColor = tappedByController ? gameData.landManaFixedColorThisTurn.get(controllerId) : null;
        if (controllerColor != null) {
            options.add(option(CONTROLLER_FIXED + controllerId,
                    "Your lands produce " + manaText(controllerColor) + " this turn"));
        }
        if (tappedByController && gameData.playersWithLandManaChoiceReplacementThisTurn.contains(controllerId)) {
            options.add(option(CONTROLLER_CHOICE + controllerId,
                    "Your lands produce one mana of a color of your choice this turn"));
        }
        ManaColor nonbasicColor = gameData.nonbasicLandsFixedManaColorThisTurn;
        if (nonbasicColor != null && !gameQueryService.hasEffectiveSupertype(gameData, land, CardSupertype.BASIC)) {
            options.add(option(NONBASIC_FIXED, "Nonbasic lands produce " + manaText(nonbasicColor) + " this turn"));
        }
        if (!gameData.landSubtypeFixedManaColorThisTurn.isEmpty()) {
            Set<CardSubtype> types = gameQueryService.effectiveBasicLandTypes(gameData, land);
            for (Map.Entry<CardSubtype, ManaColor> entry : gameData.landSubtypeFixedManaColorThisTurn.entrySet()) {
                if (types.contains(entry.getKey())) {
                    options.add(option(SUBTYPE_FIXED + entry.getKey(), entry.getKey().getDisplayName()
                            + "s produce " + manaText(entry.getValue()) + " this turn"));
                }
            }
        }
        ManaColor allLandsColor = gameData.allLandsFixedManaColorThisTurn;
        if (allLandsColor != null && event.colors().stream().anyMatch(ManaColor.COLORS::contains)) {
            options.add(option(ALL_LANDS_COLOR, "Lands produce " + manaText(allLandsColor)
                    + " instead of any other color this turn"));
        }
        if (gameData.playersWithColoredManaReplacementThisTurn.contains(event.playerId())
                && event.colors().stream().anyMatch(ManaColor.COLORS::contains)) {
            options.add(option(COLORED_TO_WHITE + event.playerId(), "Your colored mana becomes "
                    + manaText(ManaColor.WHITE) + " this turn"));
        }
        for (FloatingContinuousEffect floating : targetLandReplacements(gameData, land)) {
            ReplaceTargetLandManaWithColorEffect replacement = (ReplaceTargetLandManaWithColorEffect) floating.effect();
            if (event.colors().contains(replacement.fromColor())) {
                options.add(option(TARGET_LAND_COLOR + floating.id(), floating.sourceCardName() + ": produces "
                        + manaText(replacement.replacementColor()) + " instead of " + manaText(replacement.fromColor())));
            }
        }
        return options;
    }

    private static List<FloatingContinuousEffect> targetLandReplacements(GameData gameData, Permanent land) {
        List<FloatingContinuousEffect> replacements = new ArrayList<>();
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (land.getId().equals(floating.affectedPermanentId())
                        && floating.effect() instanceof ReplaceTargetLandManaWithColorEffect) {
                    replacements.add(floating);
                }
            }
        }
        return replacements;
    }

    @Override
    public LandManaEvent transform(GameData gameData, LandManaEvent event, ReplacementOption option) {
        String key = option.key();
        if (key.startsWith(CONTROLLER_FIXED)) {
            UUID controllerId = UUID.fromString(key.substring(CONTROLLER_FIXED.length()));
            return event.withColors(Set.of(gameData.landManaFixedColorThisTurn.get(controllerId)));
        }
        if (key.startsWith(CONTROLLER_CHOICE)) {
            return event.withColors(anyColor()).withAmount(1);
        }
        if (key.equals(NONBASIC_FIXED)) {
            return event.withColors(Set.of(gameData.nonbasicLandsFixedManaColorThisTurn));
        }
        if (key.startsWith(SUBTYPE_FIXED)) {
            CardSubtype subtype = CardSubtype.valueOf(key.substring(SUBTYPE_FIXED.length()));
            return event.withColors(Set.of(gameData.landSubtypeFixedManaColorThisTurn.get(subtype)));
        }
        if (key.startsWith(COLORED_TO_WHITE)) {
            return event.withColors(replaceColors(event.colors(), ManaColor.COLORS, ManaColor.WHITE));
        }
        if (key.startsWith(TARGET_LAND_COLOR)) {
            Permanent land = gameQueryService.findPermanentById(gameData, event.permanentId());
            ReplaceTargetLandManaWithColorEffect replacement = targetLandReplacements(gameData, land).stream()
                    .filter(floating -> key.equals(TARGET_LAND_COLOR + floating.id()))
                    .map(floating -> (ReplaceTargetLandManaWithColorEffect) floating.effect())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Mana replacement ended: " + key));
            return event.withColors(replaceColors(event.colors(), Set.of(replacement.fromColor()),
                    replacement.replacementColor()));
        }
        Set<ManaColor> colors = EnumSet.of(gameData.allLandsFixedManaColorThisTurn);
        if (event.colors().contains(ManaColor.COLORLESS)) {
            colors.add(ManaColor.COLORLESS);
        }
        return event.withColors(colors);
    }

    /** {@code colors} with each of {@code replaced} turned into {@code replacement}. */
    private static Set<ManaColor> replaceColors(Set<ManaColor> colors, Collection<ManaColor> replaced,
                                                ManaColor replacement) {
        Set<ManaColor> result = EnumSet.noneOf(ManaColor.class);
        for (ManaColor color : colors) {
            result.add(replaced.contains(color) ? replacement : color);
        }
        return result;
    }
}
