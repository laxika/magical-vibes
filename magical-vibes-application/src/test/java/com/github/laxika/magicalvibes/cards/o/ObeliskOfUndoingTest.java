package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObeliskOfUndoing.class, GrizzlyBears.class, Forest.class})
class ObeliskOfUndoingTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent you own and control to your hand")
    void bouncesOwnedAndControlledPermanent() {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new ObeliskOfUndoing());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(obelisk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent you control but do not own")
    void cannotTargetControlledButNotOwned() {
        harness.addToBattlefield(player1, new ObeliskOfUndoing());

        // A creature player1 controls but player2 owns (stolen).
        Permanent stolen = addCreatureReady(player1, new GrizzlyBears());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());

        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent you own but do not control")
    void cannotTargetOwnedButNotControlled() {
        harness.addToBattlefield(player1, new ObeliskOfUndoing());

        // A creature player1 owns but player2 controls (stolen by player2).
        Permanent lent = addCreatureReady(player2, new GrizzlyBears());
        gd.stolenCreatures.put(lent.getId(), player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target and return itself to its owner's hand")
    void canTargetItself() {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new ObeliskOfUndoing());

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, obelisk.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Obelisk of Undoing");
        harness.assertInHand(player1, "Obelisk of Undoing");
    }

    @Test
    @DisplayName("Can target a land")
    void canTargetLand() {
        harness.addToBattlefield(player1, new ObeliskOfUndoing());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not return the target after control changes before resolution")
    void targetBecomesIllegalBeforeResolution() {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new ObeliskOfUndoing());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.stolenCreatures.put(bears.getId(), player1.getId());

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles (illegal target)")).isTrue();
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(obelisk.isTapped()).isTrue();
    }
}
