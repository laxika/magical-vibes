package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.g.GoblinGoon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Clickslither.class, GoblinGoon.class, AvenEnvoy.class})
class ClickslitherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin gives Clickslither +2/+2 and trample until end of turn")
    void sacrificingGoblinBoostsClickslitherAndGrantsTrample() {
        Permanent clickslither = addCreatureReady(player1, new Clickslither());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clickslither).doesNotContain(goblin);
        assertThat(clickslither.getEffectivePower()).isEqualTo(5);
        assertThat(clickslither.getEffectiveToughness()).isEqualTo(5);
        assertThat(clickslither.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Clickslither's boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        Permanent clickslither = addCreatureReady(player1, new Clickslither());
        harness.addToBattlefield(player1, new GoblinGoon());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(clickslither.getEffectivePower()).isEqualTo(3);
        assertThat(clickslither.getEffectiveToughness()).isEqualTo(3);
        assertThat(clickslither.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The sacrifice cost only accepts Goblin creatures")
    void sacrificeCostOnlyAcceptsGoblinCreatures() {
        addCreatureReady(player1, new Clickslither());
        harness.addToBattlefield(player1, new AvenEnvoy());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The sacrifice cost only considers Goblins controlled by Clickslither's controller")
    void opponentGoblinCannotPaySacrificeCost() {
        addCreatureReady(player1, new Clickslither());
        harness.addToBattlefield(player2, new GoblinGoon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple activations stack and each sacrifice chooses one Goblin")
    void multipleActivationsStackAndChooseSeparateGoblins() {
        Permanent clickslither = addCreatureReady(player1, new Clickslither());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstGoblin.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(clickslither)
                .doesNotContain(firstGoblin, secondGoblin);
        assertThat(clickslither.getEffectivePower()).isEqualTo(7);
        assertThat(clickslither.getEffectiveToughness()).isEqualTo(7);
        assertThat(clickslither.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }
}
