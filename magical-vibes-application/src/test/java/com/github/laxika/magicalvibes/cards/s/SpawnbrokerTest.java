package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CivicWayfinder;
import com.github.laxika.magicalvibes.cards.h.HuntedLammasu;
import com.github.laxika.magicalvibes.cards.t.Terrarion;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spawnbroker.class, CivicWayfinder.class, HuntedLammasu.class, Terrarion.class, Watchwolf.class})
class SpawnbrokerTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of a creature you control and an opposing creature with equal power")
    void exchangesControlOfEligibleCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        castSpawnbroker();

        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.PucasMischiefOwnTarget.class);
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Watchwolf");
        harness.assertOnBattlefield(player1, "Watchwolf");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(opponent.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(own.getId());
    }

    @Test
    @DisplayName("Exchanges control when the opposing creature has less power")
    void exchangesControlOfLowerPowerOpponentCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CivicWayfinder());
        castSpawnbroker();

        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.PucasMischiefOwnTarget.class);
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Watchwolf");
        harness.assertOnBattlefield(player1, "Civic Wayfinder");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(opponent.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(own.getId());
    }

    @Test
    @DisplayName("Does not offer the ETB when no opposing creature is small enough")
    void noEligiblePowerPairDoesNothing() {
        harness.addToBattlefieldAndReturn(player2, new HuntedLammasu());
        castSpawnbroker();

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertOnBattlefield(player1, "Spawnbroker");
        harness.assertOnBattlefield(player2, "Hunted Lammasu");
    }

    @Test
    @DisplayName("Does not offer the ETB for a noncreature opponent permanent")
    void noNoncreatureTargetIsOffered() {
        harness.addToBattlefieldAndReturn(player2, new Terrarion());
        castSpawnbroker();

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertOnBattlefield(player1, "Spawnbroker");
        harness.assertOnBattlefield(player2, "Terrarion");
    }

    @Test
    @DisplayName("Declining the may ability leaves both creatures under their original controllers")
    void decliningExchangeLeavesControlUnchanged() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        castSpawnbroker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(own.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(opponent.getId());
    }

    @Test
    @DisplayName("Exchange has no effect when the opposing target leaves before resolution")
    void exchangeFizzlesWhenTargetLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        castSpawnbroker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(own.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(own.getId());
    }

    @Test
    @DisplayName("Exchange has no effect when the own target leaves before resolution")
    void exchangeFizzlesWhenOwnTargetLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        castSpawnbroker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        gd.playerBattlefields.get(player1.getId()).remove(own);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .doesNotContain(opponent.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(opponent.getId());
    }

    private void castSpawnbroker() {
        harness.castFromHand(player1, new Spawnbroker(), "{2}{U}");
    }
}
