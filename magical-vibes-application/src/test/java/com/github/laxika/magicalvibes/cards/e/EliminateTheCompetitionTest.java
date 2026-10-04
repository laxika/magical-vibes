package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DukharaScavenger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EliminateTheCompetition.class, DukharaScavenger.class, Forest.class})
class EliminateTheCompetitionTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void castWithSacrifices(List<UUID> targetIds, List<UUID> sacrificeIds) {
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.of(), false, null,
                null, null, null, null, false, null, null, null, sacrificeIds);
    }

    @Test
    @DisplayName("Sacrificing one creature destroys one target creature")
    void sacrificesAndDestroysMatchingX() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        castWithSacrifices(List.of(target.getId()), List.of(sacrifice.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dukhara Scavenger");
        harness.assertInGraveyard(player2, "Dukhara Scavenger");
    }

    @Test
    @DisplayName("Sacrificing no creatures destroys no targets")
    void zeroDoesNothing() {
        harness.addToBattlefield(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        castWithSacrifices(List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dukhara Scavenger");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature to set X")
    void cannotSacrificeNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(target.getId()), List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(land.getId()), List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void sacrificesArePaidBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        castWithSacrifices(List.of(target.getId()), List.of(sacrifice.getId()));

        harness.assertInGraveyard(player1, "Dukhara Scavenger");
        harness.assertNotOnBattlefield(player1, "Dukhara Scavenger");
        harness.assertOnBattlefield(player2, "Dukhara Scavenger");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Dukhara Scavenger");
    }

    @Test
    void twoSacrificesDestroyTwoCreaturesIncludingOwnCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        castWithSacrifices(List.of(ownTarget.getId(), opposingTarget.getId()),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dukhara Scavenger");
        harness.assertNotOnBattlefield(player2, "Dukhara Scavenger");
        harness.assertInGraveyard(player1, "Dukhara Scavenger");
        harness.assertInGraveyard(player2, "Dukhara Scavenger");
    }

    @Test
    void cannotChooseFewerTargetsThanSacrifices() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(target.getId()),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotInGraveyard(player1, "Dukhara Scavenger");
        harness.assertInHand(player1, "Eliminate the Competition");
    }

    @Test
    void cannotChooseNoTargetsWithPositiveX() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(), List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dukhara Scavenger");
        harness.assertInHand(player1, "Eliminate the Competition");
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(target.getId()), List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotInGraveyard(player2, "Dukhara Scavenger");
    }

    @Test
    void cannotChooseMoreTargetsThanSacrifices() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(first.getId(), second.getId()),
                List.of(sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dukhara Scavenger");
    }

    @Test
    void cannotSacrificeSameCreatureTwice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        assertThatThrownBy(() -> castWithSacrifices(List.of(first.getId(), second.getId()),
                List.of(sacrifice.getId(), sacrifice.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dukhara Scavenger");
    }

    @Test
    void sacrificedTargetDoesNotPreventOtherTargetBeingDestroyed() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DukharaScavenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaScavenger());
        harness.setHand(player1, List.of(new EliminateTheCompetition()));
        addMana();

        castWithSacrifices(List.of(first.getId(), target.getId()), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dukhara Scavenger");
        harness.assertNotOnBattlefield(player2, "Dukhara Scavenger");
        harness.assertInGraveyard(player2, "Dukhara Scavenger");
    }
}
