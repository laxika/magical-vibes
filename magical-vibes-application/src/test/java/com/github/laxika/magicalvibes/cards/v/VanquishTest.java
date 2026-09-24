package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vanquish.class, Arachnoid.class, WayfarersBauble.class})
class VanquishTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target blocking creature")
    void destroysBlockingCreature() {
        Permanent attacker = addCreatureReady(player1, new Arachnoid());
        Permanent blocker = addCreatureReady(player2, new Arachnoid());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castVanquish(blocker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Arachnoid");
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        Permanent attacker = addCreatureReady(player1, new Arachnoid());
        Permanent blocker = addCreatureReady(player2, new Arachnoid());
        Permanent bystander = addCreatureReady(player2, new Arachnoid());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThatThrownBy(() -> castVanquish(bystander))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blocker, bystander);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it is marked as blocking")
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WayfarersBauble());
        artifact.setBlocking(true);

        assertThatThrownBy(() -> castVanquish(artifact))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    @Test
    @DisplayName("Fizzles if the target is no longer blocking when Vanquish resolves")
    void fizzlesIfTargetStopsBlocking() {
        Permanent attacker = addCreatureReady(player1, new Arachnoid());
        Permanent blocker = addCreatureReady(player2, new Arachnoid());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castVanquish(blocker);
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blocker);
        harness.assertNotInGraveyard(player2, "Arachnoid");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private void castVanquish(Permanent target) {
        harness.setHand(player1, List.of(new Vanquish()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
    }

}
