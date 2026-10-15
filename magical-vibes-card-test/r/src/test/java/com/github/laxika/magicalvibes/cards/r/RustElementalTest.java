package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RustElemental.class, Ornithopter.class})
class RustElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another artifact during its controller's upkeep")
    void sacrificesAnotherArtifact() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new RustElemental());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(elemental.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot sacrifice an artifact controlled by an opponent")
    void cannotSacrificeOpponentsArtifact() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new RustElemental());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact);
        assertThat(elemental.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Taps and makes its controller lose 4 life when no other artifact can be sacrificed")
    void tapsAndLosesLifeWithoutAnotherArtifact() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new RustElemental());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
        assertThat(elemental.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Controller chooses which other artifact to sacrifice")
    void choosesOneOfMultipleArtifacts() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new RustElemental());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental, first).doesNotContain(second);
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(elemental.isTapped()).isFalse();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("An already tapped Elemental still makes its controller lose life")
    void losesLifeEvenWhenAlreadyTapped() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new RustElemental());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        elemental.tap();
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 4);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new RustElemental());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental, artifact);
        assertThat(elemental.isTapped()).isFalse();
        harness.assertLife(player1, lifeBefore);
    }
}
