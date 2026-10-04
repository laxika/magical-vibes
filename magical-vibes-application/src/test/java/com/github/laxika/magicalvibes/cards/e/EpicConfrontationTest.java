package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EpicConfrontation.class, GrizzlyBears.class, LlanowarElves.class})
class EpicConfrontationTest extends BaseCardTest {

    @Test
    @DisplayName("The boost applies before the fight")
    void boostAppliesBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EpicConfrontation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elvesId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EpicConfrontation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elvesId));

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureFirst() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EpicConfrontation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void cannotTargetOwnCreatureSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new EpicConfrontation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boosted power determines fight damage and both creatures deal damage")
    void boostedCreatureWinsFightAgainstEqualCreature() {
        Permanent ours = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EpicConfrontation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(ours.getId(), theirs.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(ours.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ours)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ours)).isEqualTo(4);
    }

    @Test
    @DisplayName("The creature still gets the boost when the opposing target leaves")
    void boostResolvesWithoutOpponentTarget() {
        Permanent ours = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EpicConfrontation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, List.of(ours.getId(), theirs.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, theirs));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ours)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ours)).isEqualTo(4);
        assertThat(ours.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Epic Confrontation");
    }

    @Test
    @DisplayName("The opposing creature takes no damage when the first target leaves")
    void noFightWithoutControllerTarget() {
        Permanent ours = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EpicConfrontation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, List.of(ours.getId(), theirs.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ours));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(theirs.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Epic Confrontation");
    }

    @Test
    @DisplayName("An opposing target that changes to your control does not fight")
    void noFightWhenOpponentTargetChangesController() {
        Permanent ours = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EpicConfrontation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, List.of(ours.getId(), theirs.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(theirs);
        gd.playerBattlefields.get(player1.getId()).add(theirs);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ours)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ours)).isEqualTo(4);
        assertThat(ours.getMarkedDamage()).isZero();
        assertThat(theirs.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }
}
