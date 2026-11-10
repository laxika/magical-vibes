package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.FinalChapterTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Reads the final-chapter abilities (a {@link FinalChapterTrigger}: the final chapter ability of a Saga you control
 * triggers or resolves, CR 715.2e), as {@link WatchedAbilities} finds them, for the {@link SagaWatch} collectors, which
 * have no legacy view of them. The collectors judge the ability's
 * only trigger-level part from the ability itself: "triggers only once each turn" ({@link SagaEffect#limit()}, marked
 * by {@link TriggeredAbility#limitKey()} on the source). The ability rejects every other option as it is built, as the
 * collectors read none.
 */
public final class SagaAbilityReader {

    private SagaAbilityReader() {
    }

    /**
     * One effect of a final-chapter ability as its collector reads it.
     *
     * @param effect  the effect, as it is written
     * @param ability the ability the effect belongs to
     */
    public record SagaEffect(CardEffect effect, TriggeredAbility ability) {

        public SagaEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        /** Whether the ability "triggers only once each turn", so nothing makes it trigger additional times. */
        public boolean triggersOnlyOnceEachTurn() {
            return limit() == TriggerLimit.ONCE_EACH_TURN;
        }

        /** This effect read as {@code effect} instead, as a collector transforms it. */
        public SagaEffect withEffect(CardEffect effect) {
            return new SagaEffect(effect, ability);
        }

        /** Whether the ability's limit keeps it from triggering for {@code sourceId} now. */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit as used by {@code sourceId}. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, SagaWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /**
     * The effects of {@code ability}, as its collector reads them for the condition watched as {@code watched}.
     */
    public static List<SagaEffect> read(TriggeredAbility ability, Trigger watched) {
        if (!ability.hasWatchedTrigger(watched)) {
            throw new IllegalArgumentException(ability + " has no final-chapter condition watched as " + watched);
        }
        List<SagaEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            effects.add(new SagaEffect(registration.effect(), ability));
        }
        return effects;
    }
}
