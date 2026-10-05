package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaulfistSquad.class, TidyConclusion.class})
class MaulfistSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Maulfist Squad")
    void fabricateCountersMode() {
        castMaulfistSquad(0);
        resolveAllTriggers();

        Permanent squad = findPermanent(player1, "Maulfist Squad");

        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castMaulfistSquad(1);
        resolveAllTriggers();

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(1);
        Permanent servo = servos.getFirst();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fabricate chooses whether to put counters on the creature only as the trigger resolves")
    void fabricateChoiceIsMadeOnResolution() {
        castMaulfistSquad(0);
        harness.passBothPriorities();

        Permanent squad = findPermanent(player1, "Maulfist Squad");
        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Create a 1/1 colorless Servo artifact creature token");
        resolveAllTriggers();

        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Servo");
    }

    @Test
    @DisplayName("Fabricate creates a Servo if Maulfist Squad leaves before its trigger resolves")
    void fabricateCreatesServoWhenSourceLeaves() {
        castMaulfistSquad(0);
        harness.passBothPriorities();
        Permanent squad = findPermanent(player1, "Maulfist Squad");

        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, squad.getId());
        harness.assertInGraveyard(player1, "Maulfist Squad");

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Servo");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Menace rejects one blocker and permits two blockers")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new MaulfistSquad());
        addCreatureReady(player2, new MaulfistSquad());
        addCreatureReady(player2, new MaulfistSquad());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.assertInGraveyard(player1, "Maulfist Squad");
    }

    private void castMaulfistSquad(int mode) {
        harness.setHand(player1, List.of(new MaulfistSquad()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, mode);
    }
}
