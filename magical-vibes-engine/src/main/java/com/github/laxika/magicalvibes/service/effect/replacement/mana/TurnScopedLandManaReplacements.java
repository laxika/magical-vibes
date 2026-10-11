package com.github.laxika.magicalvibes.service.effect.replacement.mana;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Land-mana replacements that resolving spells and abilities create until end of turn, recorded
 * on {@link GameData}:
 * <ul>
 *   <li>Deep Water — lands a player controls produce one fixed color
 *       ({@code landManaFixedColorThisTurn});</li>
 *   <li>Harvest Mage — lands a player taps produce one mana of a color of their choice instead of
 *       any other type and amount ({@code playersWithLandManaChoiceReplacementThisTurn});</li>
 *   <li>Pale Moon — nonbasic lands produce a fixed type ({@code nonbasicLandsFixedManaColorThisTurn});</li>
 *   <li>Chaos Moon — lands of a basic land type produce a fixed type
 *       ({@code landSubtypeFixedManaColorThisTurn});</li>
 *   <li>Hall of Gemstone — lands produce the chosen color instead of any other <em>color</em>, so
 *       it leaves colorless mana alone ({@code allLandsFixedManaColorThisTurn}).</li>
 * </ul>
 */
@Component
class TurnScopedLandManaReplacements extends LandManaCandidateSource {

    private static final String CONTROLLER_FIXED = "ControllerLandsFixedColor:";
    private static final String CONTROLLER_CHOICE = "ControllerLandsChosenColor:";
    private static final String NONBASIC_FIXED = "NonbasicLandsFixedColor";
    private static final String SUBTYPE_FIXED = "LandSubtypeFixedColor:";
    private static final String ALL_LANDS_COLOR = "AllLandsChosenColor";

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
        return options;
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
        Set<ManaColor> colors = EnumSet.of(gameData.allLandsFixedManaColorThisTurn);
        if (event.colors().contains(ManaColor.COLORLESS)) {
            colors.add(ManaColor.COLORLESS);
        }
        return event.withColors(colors);
    }
}
