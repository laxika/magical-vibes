package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiltLeafArchdruid.class, Forest.class, Island.class, GrizzlyBears.class, WordOfSeizing.class})
class GiltLeafArchdruidTest extends BaseCardTest {

    private void setUpControllerMain() {
        harness.addToBattlefield(player1, new GiltLeafArchdruid());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting a Druid spell offers a draw; accepting draws a card")
    void castingDruidSpellAcceptDraws() {
        setUpControllerMain();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GiltLeafArchdruid()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Declining the trigger draws no card")
    void castingDruidSpellDeclineNoDraw() {
        setUpControllerMain();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GiltLeafArchdruid()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Island");
    }

    @Test
    @DisplayName("Casting a non-Druid spell does not trigger")
    void castingNonDruidSpellDoesNotTrigger() {
        setUpControllerMain();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Tapping seven Druids gains control of all lands the target player controls")
    void gainControlOfAllLands() {
        // Source Druid at index 0, plus six more Druids = seven untapped Druids (auto-tapped as cost).
        Permanent source = addCreatureReady(player1, new GiltLeafArchdruid());
        for (int i = 0; i < 6; i++) {
            addCreatureReady(player1, new GiltLeafArchdruid());
        }

        Permanent forestA = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent forestB = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIdx, 0, player2.getId());
        harness.passBothPriorities();

        // Both lands moved to player1; the creature stayed with player2.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forestA.getId()))
                .anyMatch(p -> p.getId().equals(forestB.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(forestA.getId()))
                .noneMatch(p -> p.getId().equals(forestB.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot activate the ability with fewer than seven untapped Druids")
    void cannotActivateWithoutSevenDruids() {
        Permanent source = addCreatureReady(player1, new GiltLeafArchdruid());
        // Only six Druids total (source + five).
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new GiltLeafArchdruid());
        }

        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> harness.activateAbility(player1, sourceIdx, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
    }

    @Test
    void summoningSickDruidsCanPayCostAndLandsAreChosenAtResolution() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new GiltLeafArchdruid());
        }
        harness.activateAbility(player1, 0, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(land.isTapped()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
    }

    @Test
    void targetingYourselfMakesTemporaryLandControlPermanent() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new GiltLeafArchdruid());
        }
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WordOfSeizing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, land.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.activateAbility(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
    }

    @Test
    void opponentsDruidSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GiltLeafArchdruid());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiltLeafArchdruid()));
        harness.addMana(player2, ManaColor.GREEN, 5);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Gilt-Leaf Archdruid");
    }

    @Test
    void tappedAndOpposingDruidsCannotPayCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new GiltLeafArchdruid());
        }
        harness.addToBattlefieldAndReturn(player1, new GiltLeafArchdruid()).tap();
        harness.addToBattlefield(player2, new GiltLeafArchdruid());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).subList(0, 6))
                .noneMatch(Permanent::isTapped);
    }
}
