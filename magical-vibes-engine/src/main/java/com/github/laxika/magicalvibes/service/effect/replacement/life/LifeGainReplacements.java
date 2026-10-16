package com.github.laxika.magicalvibes.service.effect.replacement.life;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.replacement.LifeGainEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Life-gain replacements on permanents the gaining player controls, one option per effect
 * instance: {@code additional life} (Heron of Hope) and {@code multiplies the life gained} (Boon
 * Reflection). Both only rewrite the amount, so they are pure; the gaining player is prompted only
 * when the order in which they apply changes the amount gained (CR 616.1).
 */
@Component
public class LifeGainReplacements implements ReplacementCandidateSource<LifeGainEvent> {

    private static final String KEY = "LifeGain:";

    private final GameQueryService gameQueryService;

    public LifeGainReplacements(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    private record SourcedEffect(Permanent source, int index, LifeGainReplacementEffect effect) {

        String key() {
            return KEY + source.getId() + ":" + index;
        }
    }

    @Override
    public Class<LifeGainEvent> eventType() {
        return LifeGainEvent.class;
    }

    @Override
    public List<ReplacementOption> applicable(GameData gameData, LifeGainEvent event) {
        if (event.amount() <= 0) {
            return List.of();
        }
        List<ReplacementOption> options = new ArrayList<>();
        for (SourcedEffect sourced : activeEffects(gameData, event)) {
            String text = sourced.effect().additionalLifeGain() != 0
                    ? "gain " + sourced.effect().additionalLifeGain() + " additional life"
                    : "multiply the life gained by "
                    + MaroGoneNutsSupport.apply(gameData, sourced.effect(), sourced.effect().lifeGainMultiplier());
            options.add(new ReplacementOption(sourced.key(),
                    sourced.source().getCard().getName() + ": " + text,
                    ReplacementOption.Kind.REPLACEMENT, true));
        }
        return options;
    }

    @Override
    public LifeGainEvent transform(GameData gameData, LifeGainEvent event, ReplacementOption option) {
        SourcedEffect sourced = activeEffects(gameData, event).stream()
                .filter(candidate -> candidate.key().equals(option.key()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Life gain replacement source left: " + option.key()));
        int amount = event.amount() + sourced.effect().additionalLifeGain();
        amount *= MaroGoneNutsSupport.apply(gameData, sourced.effect(), sourced.effect().lifeGainMultiplier());
        return event.withAmount(amount);
    }

    private List<SourcedEffect> activeEffects(GameData gameData, LifeGainEvent event) {
        List<SourcedEffect> effects = new ArrayList<>();
        for (Permanent source : gameData.playerBattlefields.getOrDefault(event.playerId(), List.of())) {
            List<CardEffect> statics = gameQueryService.getActiveStaticEffects(gameData, source);
            for (int i = 0; i < statics.size(); i++) {
                if (statics.get(i) instanceof LifeGainReplacementEffect replacement) {
                    effects.add(new SourcedEffect(source, i, replacement));
                }
            }
        }
        return effects;
    }
}
