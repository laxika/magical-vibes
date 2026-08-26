package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spawnbroker.class, GrizzlyBears.class, HillGiant.class})
class SpawnbrokerTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of a creature you control and an opposing creature with equal power")
    void exchangesControlOfEligibleCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSpawnbroker();

        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.PucasMischiefOwnTarget.class);
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(opponent.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(own.getId());
    }

    @Test
    @DisplayName("Does not offer the ETB when no opposing creature is small enough")
    void noEligiblePowerPairDoesNothing() {
        harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castSpawnbroker();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spawnbroker");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Declining the may ability leaves both creatures under their original controllers")
    void decliningExchangeLeavesControlUnchanged() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

    private void castSpawnbroker() {
        harness.setHand(player1, List.of(new Spawnbroker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
