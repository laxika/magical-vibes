package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SilhanaLedgewalker;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladegriffPrototype.class, GrizzlyBears.class, Mountain.class,
        DarksteelIngot.class, SilhanaLedgewalker.class, SolRing.class})
class BladegriffPrototypeTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged player chooses a nonland permanent controlled by an opponent")
    void damagedPlayerChoosesOpponentControlledNonlandPermanent() {
        Permanent bladegriff = addCreatureReady(player1, new BladegriffPrototype());
        bladegriff.setAttacking(true);
        Permanent ownPermanent = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(target.getId())
                .doesNotContain(ownPermanent.getId(), land.getId());
    }

    @Test
    @DisplayName("The chosen nonland permanent is destroyed")
    void destroysChosenPermanent() {
        Permanent bladegriff = addCreatureReady(player1, new BladegriffPrototype());
        bladegriff.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The damaged player can choose a noncreature artifact among multiple targets")
    void destroysOnlyTheChosenArtifact() {
        Permanent bladegriff = addCreatureReady(player1, new BladegriffPrototype());
        bladegriff.setAttacking(true);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SolRing());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);

        harness.handlePermanentChosen(player2, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player2, "Sol Ring");
    }

    @Test
    @DisplayName("An indestructible permanent is a legal choice but survives destruction")
    void indestructibleTargetSurvives() {
        Permanent bladegriff = addCreatureReady(player1, new BladegriffPrototype());
        bladegriff.setAttacking(true);
        Permanent ingot = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ingot.getId(), ring.getId());
        harness.handlePermanentChosen(player2, ingot.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Ingot");
        harness.assertNotInGraveyard(player2, "Darksteel Ingot");
        harness.assertOnBattlefield(player2, "Sol Ring");
    }

    @Test
    @DisplayName("The damaged player cannot choose their hexproof creature for an opponent's ability")
    void hexproofUsesAbilityControllerRatherThanTargetChooser() {
        Permanent bladegriff = addCreatureReady(player1, new BladegriffPrototype());
        bladegriff.setAttacking(true);
        Permanent ledgewalker = addCreatureReady(player2, new SilhanaLedgewalker());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(ring.getId()).doesNotContain(ledgewalker.getId());
        harness.handlePermanentChosen(player2, ring.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Silhana Ledgewalker");
        harness.assertInGraveyard(player2, "Sol Ring");
    }

    @Test
    @DisplayName("Combat damage with no legal target does not prompt or destroy a friendly permanent")
    void noLegalTargetLeavesFriendlyPermanentsAlone() {
        Permanent bladegriff = addCreatureReady(player1, new BladegriffPrototype());
        bladegriff.setAttacking(true);
        harness.addToBattlefield(player1, new SolRing());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bladegriff Prototype");
        harness.assertOnBattlefield(player1, "Sol Ring");
    }
}
