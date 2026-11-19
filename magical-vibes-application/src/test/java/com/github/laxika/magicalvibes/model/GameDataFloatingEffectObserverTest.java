package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameDataFloatingEffectObserverTest {

    private GameData gd;
    private final List<String> reported = new ArrayList<>();

    @BeforeEach
    void setUp() {
        gd = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "Player1");
        gd.rulesObserver = new RulesObserver() {
            @Override
            public void floatingEffectBegan(FloatingContinuousEffect effect) {
                reported.add("began " + effect.sourceCardName());
            }

            @Override
            public void floatingEffectEnded(FloatingContinuousEffect effect) {
                reported.add("ended " + effect.sourceCardName());
            }
        };
    }

    private static FloatingContinuousEffect floating(String source, Duration duration) {
        return new FloatingContinuousEffect(UUID.randomUUID(), source, null, UUID.randomUUID(),
                new BoostTargetCreatureEffect(1, 1), UUID.randomUUID(), null, null, duration, 0);
    }

    @Test
    @DisplayName("An added floating effect is reported as it begins, stamped")
    void addedEffectIsReported() {
        FloatingContinuousEffect stamped = gd.addFloatingEffect(floating("Giant Growth", Duration.UNTIL_END_OF_TURN));

        assertThat(stamped.timestamp()).isPositive();
        assertThat(reported).containsExactly("began Giant Growth");
    }

    @Test
    @DisplayName("A floating effect stored with its own timestamp is reported and keeps that timestamp")
    void putEffectKeepsItsTimestamp() {
        FloatingContinuousEffect effect = floating("Control Magic", Duration.WHILE_ATTACHED).withTimestamp(7);

        gd.putFloatingEffect(effect);

        assertThat(gd.floatingEffects).containsExactly(effect);
        assertThat(reported).containsExactly("began Control Magic");
    }

    @Test
    @DisplayName("Expiring reports only the effects whose duration ended")
    void expiringReportsEachEndedEffect() {
        gd.addFloatingEffect(floating("Giant Growth", Duration.UNTIL_END_OF_TURN));
        gd.addFloatingEffect(floating("Jade Statue", Duration.UNTIL_END_OF_COMBAT));
        reported.clear();

        gd.expireFloatingEffects(fe -> Duration.UNTIL_END_OF_COMBAT.equals(fe.duration()));

        assertThat(reported).containsExactly("ended Jade Statue");
        assertThat(gd.floatingEffects).extracting(FloatingContinuousEffect::sourceCardName)
                .containsExactly("Giant Growth");
    }

    @Test
    @DisplayName("Ending one effect leaves an equal-looking other one in place")
    void endingOneEffect() {
        FloatingContinuousEffect first = gd.addFloatingEffect(floating("Giant Growth", Duration.UNTIL_END_OF_TURN));
        gd.addFloatingEffect(floating("Giant Growth", Duration.UNTIL_END_OF_TURN));
        reported.clear();

        gd.endFloatingEffect(first);

        assertThat(reported).containsExactly("ended Giant Growth");
        assertThat(gd.floatingEffects).hasSize(1).doesNotContain(first);
    }

    @Test
    @DisplayName("Replacing an effect keeps its place and reports the old one ending and the new one beginning")
    void replacingKeepsPlace() {
        FloatingContinuousEffect old = gd.addFloatingEffect(floating("Old", Duration.PERMANENT));
        FloatingContinuousEffect later = gd.addFloatingEffect(floating("Later", Duration.PERMANENT));
        FloatingContinuousEffect replacement = floating("New", Duration.PERMANENT).withTimestamp(old.timestamp());
        reported.clear();

        gd.replaceFloatingEffect(old, replacement);

        assertThat(gd.floatingEffects).containsExactly(replacement, later);
        assertThat(reported).containsExactly("ended Old", "began New");
    }

    @Test
    @DisplayName("Nothing is reported while rules events are suppressed")
    void suppressedReportsNothing() {
        gd.rulesEvents.suppress();

        gd.addFloatingEffect(floating("Giant Growth", Duration.UNTIL_END_OF_TURN));
        gd.expireFloatingEffects(fe -> Duration.UNTIL_END_OF_TURN.equals(fe.duration()));

        assertThat(reported).isEmpty();
    }

    @Test
    @DisplayName("A \"for as long as\" duration ends at no event (CR 611.2b)")
    void asLongAsEndsAtNoEvent() {
        assertThat(Duration.WHILE_SOURCE_TAPPED.end()).isEqualTo(Duration.End.NEVER);
        assertThatThrownBy(() -> new Duration(Duration.End.END_OF_TURN, Duration.AsLongAs.SOURCE_TAPPED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Each \"skip your next\" effect is used up as it applies, a this-turn one first (CR 614.10a)")
    void skipNextEffectsAreUsedUpOneByOne() {
        UUID player = UUID.randomUUID();
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Blinding Angel", null, player,
                new SkipNextEffect(SkipKind.COMBAT_PHASE), player, Duration.PERMANENT));
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Moment of Silence", null, player,
                new SkipNextEffect(SkipKind.COMBAT_PHASE, Subjects.targetPlayer(), false, true), player,
                Duration.UNTIL_END_OF_TURN));
        reported.clear();

        assertThat(gd.skipNextCount(player, SkipKind.COMBAT_PHASE)).isEqualTo(2);
        assertThat(gd.applySkipNext(player, SkipKind.DRAW_STEP)).isFalse();
        assertThat(gd.applySkipNext(player, SkipKind.COMBAT_PHASE)).isTrue();
        assertThat(gd.applySkipNext(player, SkipKind.COMBAT_PHASE)).isTrue();
        assertThat(gd.applySkipNext(player, SkipKind.COMBAT_PHASE)).isFalse();
        assertThat(reported).containsExactly("ended Moment of Silence", "ended Blinding Angel");
    }

    @Test
    @DisplayName("Only a duration that begins at a combat phase names one")
    void onlyACombatPhaseStartNamesACombatPhase() {
        assertThat(Duration.duringCombatPhase(2).laterCombatPhase(1)).isEqualTo(Duration.duringCombatPhase(3));
        assertThatThrownBy(() -> new Duration(Duration.Start.COMBAT_PHASE_OF_THIS_TURN, Duration.End.END_OF_COMBAT,
                null)).isInstanceOf(IllegalArgumentException.class);
    }
}
