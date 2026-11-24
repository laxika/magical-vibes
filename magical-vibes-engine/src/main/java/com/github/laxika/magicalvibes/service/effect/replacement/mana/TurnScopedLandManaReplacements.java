package com.github.laxika.magicalvibes.service.effect.replacement.mana;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceColoredManaWithWhiteUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * False Dawn's land-mana replacement, a floating effect on the player a resolved spell made: abilities that
 * player controls that would add colored mana add that much white mana instead. The floated
 * {@code ReplaceLandManaWithColorEffect}s are {@link LandManaColorReplacements}'.
 */
@Component
class TurnScopedLandManaReplacements extends LandManaCandidateSource {

    private static final String COLORED_TO_WHITE = "ColoredManaBecomesWhite:";

    TurnScopedLandManaReplacements(GameQueryService gameQueryService) {
        super(gameQueryService);
    }

    @Override
    protected List<ReplacementOption> applicable(GameData gameData, LandManaEvent event, Permanent land) {
        List<ReplacementOption> options = new ArrayList<>();
        if (gameData.hasFloatingEffectOnPlayer(event.playerId(), ReplaceColoredManaWithWhiteUntilEndOfTurnEffect.class)
                && event.colors().stream().anyMatch(ManaColor.COLORS::contains)) {
            options.add(option(COLORED_TO_WHITE + event.playerId(), "Your colored mana becomes "
                    + manaText(ManaColor.WHITE) + " this turn"));
        }
        return options;
    }

    @Override
    public LandManaEvent transform(GameData gameData, LandManaEvent event, ReplacementOption option) {
        return event.withColors(replaceColors(event.colors(), ManaColor.COLORS, ManaColor.WHITE));
    }
}
