package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PearlLakeAncient.class, Cancel.class, Forest.class, Island.class, Shock.class})
class PearlLakeAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast with flash and cannot be countered")
    void castsWithFlashAndCannotBeCountered() {
        PearlLakeAncient ancient = new PearlLakeAncient();
        harness.setHand(player1, List.of(ancient));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, ancient.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pearl Lake Ancient");
        harness.assertNotInGraveyard(player1, "Pearl Lake Ancient");
    }

    @Test
    @DisplayName("Prowess boosts it for each noncreature spell until end of turn")
    void prowessBoostsUntilEndOfTurn() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());
        int basePower = gqs.getEffectivePower(gd, ancient);
        int baseToughness = gqs.getEffectiveToughness(gd, ancient);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ancient)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, ancient)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Returns three controlled lands and itself to hand")
    void returnsThreeLandsAndItselfToHand() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ancient), null, null);
        harness.passBothPriorities();

        assertThat(countLands(player1)).isZero();
        harness.assertNotOnBattlefield(player1, "Pearl Lake Ancient");
        harness.assertInHand(player1, "Pearl Lake Ancient");
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.hasType(CardType.LAND))
                .count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without three lands it controls")
    void cannotActivateWithoutThreeControlledLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ancient),
                null,
                null
        )).isInstanceOf(IllegalStateException.class);

        assertThat(countLands(player1)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Pearl Lake Ancient");
    }

    @Test
    @DisplayName("Each noncreature cast adds a separate prowess boost")
    void prowessStacksForMultipleSpells() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());
        int basePower = gqs.getEffectivePower(gd, ancient);
        int baseToughness = gqs.getEffectiveToughness(gd, ancient);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower + 1);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, ancient)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Prowess still resolves when the triggering spell is countered")
    void prowessSurvivesCounteringTheSpell() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());
        int basePower = gqs.getEffectivePower(gd, ancient);
        int baseToughness = gqs.getEffectiveToughness(gd, ancient);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, opponentLife);
        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ancient)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentsSpellDoesNotTriggerProwess() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());
        int basePower = gqs.getEffectivePower(gd, ancient);
        int baseToughness = gqs.getEffectiveToughness(gd, ancient);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, ancient)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Creature spells and playing lands do not trigger prowess")
    void creaturesAndLandsDoNotTriggerProwess() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());
        int basePower = gqs.getEffectivePower(gd, ancient);
        int baseToughness = gqs.getEffectiveToughness(gd, ancient);
        harness.setHand(player1, List.of(new Island(), new PearlLakeAncient()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ancient)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, ancient)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Tapped lands are returned as a cost before the Ancient returns")
    void tappedLandsArePaidBeforeResolution() {
        harness.setHand(player1, List.of());
        for (int i = 0; i < 3; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
            land.tap();
        }
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());
        ancient.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ancient), null, null);

        assertThat(countLands(player1)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Pearl Lake Ancient");
        harness.assertNotInHand(player1, "Pearl Lake Ancient");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Pearl Lake Ancient");
        harness.assertNotOnBattlefield(player1, "Pearl Lake Ancient");
    }

    @Test
    @DisplayName("The controller chooses exactly three lands when more are available")
    void choosesThreeOfFourLands() {
        harness.setHand(player1, List.of());
        Permanent retained = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new PearlLakeAncient());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ancient), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(retained, ancient);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(retained);
        harness.assertInHand(player1, "Pearl Lake Ancient");
    }

    private long countLands(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }
}
