package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VortexRunner.class, Forest.class})
class VortexRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the bonus below eight lands")
    void noBonusBelowEightLands() {
        addLands(player1, 7);
        harness.addToBattlefield(player1, new VortexRunner());

        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and can't be blocked at eight lands")
    void getsBonusAtEightLands() {
        addLands(player1, 8);
        harness.addToBattlefield(player1, new VortexRunner());

        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, runner)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when the controller drops below eight lands")
    void losesBonusWhenLandsDrop() {
        addLands(player1, 8);
        harness.addToBattlefield(player1, new VortexRunner());

        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();
    }

    @Test
    @DisplayName("Opponent's lands do not count")
    void opponentsLandsDoNotCount() {
        addLands(player2, 8);
        harness.addToBattlefield(player1, new VortexRunner());

        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();
    }

    @Test
    @DisplayName("The bonus appears immediately when the eighth land enters, even if all lands are tapped")
    void gainsBonusWhenEighthLandEnters() {
        addLands(player1, 7);
        harness.addToBattlefield(player1, new VortexRunner());
        Permanent runner = findRunner();

        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();

        addLands(player1, 1);
        findPermanents(player1, "Forest").forEach(land -> land.tap());

        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, runner)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isTrue();
    }

    @Test
    @DisplayName("More than eight lands still gives only +1/+0, even with multiple runners")
    void bonusDoesNotScaleWithLandOrRunnerCount() {
        addLands(player1, 9);
        harness.addToBattlefield(player1, new VortexRunner());
        harness.addToBattlefield(player1, new VortexRunner());

        for (Permanent runner : findPermanents(player1, "Vortex Runner")) {
            assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, runner)).isEqualTo(3);
            assertThat(gqs.hasCantBeBlocked(gd, runner)).isTrue();
        }
    }

    @Test
    @DisplayName("Nonland permanents do not contribute to the eight-land threshold")
    void nonlandsDoNotCount() {
        addLands(player1, 7);
        harness.addToBattlefield(player1, new VortexRunner());
        harness.addToBattlefield(player1, new VortexRunner());

        for (Permanent runner : findPermanents(player1, "Vortex Runner")) {
            assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
            assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();
        }
    }

    @Test
    @DisplayName("The ability counts the current controller's lands after control changes")
    void usesCurrentControllersLands() {
        addLands(player1, 8);
        addLands(player2, 7);
        harness.addToBattlefield(player1, new VortexRunner());
        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(runner);
        gd.playerBattlefields.get(player2.getId()).add(runner);

        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();

        addLands(player2, 1);

        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isTrue();
    }

    @Test
    @DisplayName("A creature cannot block the runner at eight lands")
    void cannotBeBlockedAtEightLands() {
        Permanent attacker = addCreatureReady(player1, new VortexRunner());
        attacker.setAttacking(true);
        addLands(player1, 8);
        addCreatureReady(player2, new VortexRunner());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("A creature can block the runner below eight lands")
    void canBeBlockedBelowEightLands() {
        Permanent attacker = addCreatureReady(player1, new VortexRunner());
        attacker.setAttacking(true);
        addLands(player1, 7);
        Permanent blocker = addCreatureReady(player2, new VortexRunner());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private Permanent findRunner() {
        return findPermanent(player1, "Vortex Runner");
    }
}
