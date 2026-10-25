package com.github.laxika.magicalvibes.service.effect.replacement.mill;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerOpponentMillBonusEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerOpponentMillMultiplyingEffect;
import com.github.laxika.magicalvibes.model.replacement.MillEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Replacement effects that change how many cards an opponent of their controller mills, one option
 * per effect instance: The Water Crystal's "that many cards plus four" and Bruvac the
 * Grandiloquent's "twice that many". Both apply only to a mill of one or more cards. They only
 * rewrite the number, so they are pure; the milling player is prompted only when the order in
 * which they apply changes how many cards are milled (CR 616.1).
 */
@Component
public class MillReplacements implements ReplacementCandidateSource<MillEvent> {

    private final GameQueryService gameQueryService;

    public MillReplacements(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    private record MillReplacement(String key, String label, CardEffect effect) {}

    @Override
    public Class<MillEvent> eventType() {
        return MillEvent.class;
    }

    @Override
    public List<ReplacementOption> applicable(GameData gameData, MillEvent event) {
        return replacements(gameData, event).stream()
                .map(replacement -> new ReplacementOption(replacement.key(), replacement.label(),
                        ReplacementOption.Kind.REPLACEMENT, true))
                .toList();
    }

    @Override
    public MillEvent transform(GameData gameData, MillEvent event, ReplacementOption option) {
        CardEffect effect = replacements(gameData, event).stream()
                .filter(replacement -> replacement.key().equals(option.key()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Mill replacement source left: " + option.key()))
                .effect();
        return switch (effect) {
            case ControllerOpponentMillBonusEffect bonus ->
                    event.withAmount(SaturatingMath.add(event.amount(), bonus.amount()));
            case ControllerOpponentMillMultiplyingEffect multiplier ->
                    event.withAmount(SaturatingMath.multiply(event.amount(), multiplier.millMultiplier()));
            default -> throw new IllegalStateException("Not a mill replacement: " + effect);
        };
    }

    private List<MillReplacement> replacements(GameData gameData, MillEvent event) {
        List<MillReplacement> replacements = new ArrayList<>();
        if (event.amount() <= 0) {
            return replacements;
        }
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (controllerId.equals(event.playerId())
                    || permanent.isFaceDown()
                    || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
                return;
            }
            List<CardEffect> effects = gameQueryService.getActiveStaticEffects(gameData, permanent);
            for (int i = 0; i < effects.size(); i++) {
                CardEffect effect = effects.get(i);
                String key = "Mill:" + permanent.getId() + ":" + i;
                String name = permanent.getCard().getName();
                if (effect instanceof ControllerOpponentMillBonusEffect bonus) {
                    replacements.add(new MillReplacement(key,
                            name + ": mill " + bonus.amount() + " more", effect));
                } else if (effect instanceof ControllerOpponentMillMultiplyingEffect multiplier) {
                    replacements.add(new MillReplacement(key,
                            name + ": mill " + (multiplier.millMultiplier() == 2 ? "twice"
                                    : multiplier.millMultiplier() + " times") + " that many", effect));
                }
            }
        });
        return replacements;
    }
}
