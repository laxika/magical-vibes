package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AltarGolem.class, GrizzlyBears.class})
class AltarGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures on the battlefield")
    void ptEqualsCreatureCount() {
        Permanent golem = addCreatureReady(player1, new AltarGolem());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Golem + two Grizzly Bears = 3 creatures.
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapped Altar Golem does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent golem = addCreatureReady(player1, new AltarGolem());
        golem.tap();

        harness.performUntapStep(player1);

        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping five untapped creatures untaps Altar Golem")
    void tapFiveCreaturesUntapsGolem() {
        Permanent golem = addCreatureReady(player1, new AltarGolem());
        golem.tap();
        // Exactly five untapped creatures; the cost auto-pays by tapping all of them.
        List<Permanent> fodder = addReadyCreatures(player1, 5);

        int golemIdx = gd.playerBattlefields.get(player1.getId()).indexOf(golem);
        harness.activateAbility(player1, golemIdx, null, null);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isFalse();
        assertThat(fodder).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot activate with fewer than five untapped creatures")
    void cannotActivateWithFewerThanFive() {
        Permanent golem = addCreatureReady(player1, new AltarGolem());
        golem.tap();
        // Only 4 untapped creatures available besides the tapped golem.
        addReadyCreatures(player1, 4);

        int golemIdx = gd.playerBattlefields.get(player1.getId()).indexOf(golem);
        assertThatThrownBy(() -> harness.activateAbility(player1, golemIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power and toughness update as creatures enter and leave")
    void creatureCountUpdates() {
        Permanent golem = addCreatureReady(player1, new AltarGolem());
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(1);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.tap();
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerGraveyards.get(player2.getId()).add(opponentCreature.getCard());
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning-sick Golem can tap itself and four summoning-sick creatures")
    void summoningSickCreaturesIncludingSourceCanPayCost() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new AltarGolem());
        List<Permanent> fodder = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()))
                .toList();

        harness.activateAbility(player1, 0, null, null);

        assertThat(golem.isTapped()).isTrue();
        assertThat(fodder).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        assertThat(golem.isTapped()).isFalse();
        assertThat(fodder).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Opponent's creatures and tapped creatures cannot pay the cost")
    void opponentAndTappedCreaturesCannotPayCost() {
        Permanent golem = addCreatureReady(player1, new AltarGolem());
        golem.tap();
        List<Permanent> ownCreatures = addReadyCreatures(player1, 5);
        ownCreatures.getFirst().tap();
        List<Permanent> opponentCreatures = addReadyCreatures(player2, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ownCreatures.subList(1, 5)).noneMatch(Permanent::isTapped);
        assertThat(opponentCreatures).noneMatch(Permanent::isTapped);
        assertThat(golem.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller chooses exactly five creatures when more are available")
    void choosesFiveFromMoreThanFive() {
        Permanent golem = addCreatureReady(player1, new AltarGolem());
        golem.tap();
        List<Permanent> fodder = addReadyCreatures(player1, 6);

        harness.activateAbility(player1, 0, null, null);
        for (Permanent creature : fodder.subList(1, 6)) {
            harness.handlePermanentChosen(player1, creature.getId());
        }

        assertThat(fodder.getFirst().isTapped()).isFalse();
        assertThat(fodder.subList(1, 6)).allMatch(Permanent::isTapped);
        assertThat(golem.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(golem.isTapped()).isFalse();
        assertThat(fodder.getFirst().isTapped()).isFalse();
    }

    private List<Permanent> addReadyCreatures(Player player, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> addCreatureReady(player, new GrizzlyBears()))
                .toList();
    }
}
