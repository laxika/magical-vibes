package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakshasaGravecaller.class, GrizzlyBears.class})
class RakshasaGravecallerTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not create Zombie tokens")
    void decliningExploitDoesNothing() {
        castRakshasaGravecaller();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.assertOnBattlefield(player1, "Rakshasa Gravecaller");
    }

    @Test
    @DisplayName("Exploiting a creature creates two Zombie tokens")
    void exploitCreatesTwoZombies() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());

        castRakshasaGravecaller();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Rakshasa Gravecaller");
    }

    private void castRakshasaGravecaller() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RakshasaGravecaller()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
