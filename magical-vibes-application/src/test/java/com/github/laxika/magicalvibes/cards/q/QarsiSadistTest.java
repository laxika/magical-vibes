package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.d.DeathWind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QarsiSadist.class, ColossodonYearling.class, DeathWind.class})
class QarsiSadistTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not drain life")
    void decliningExploitDoesNothing() {
        castQarsiSadist();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Qarsi Sadist");
    }

    @Test
    @DisplayName("Exploiting a creature makes target opponent lose 2 life and the controller gain 2 life")
    void exploitDrainsTargetOpponent() {
        Permanent sacrifice = addCreatureReady(player1, new ColossodonYearling());

        castQarsiSadist();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Colossodon Yearling");
    }

    @Test
    @DisplayName("Exploit trigger cannot target its controller")
    void exploitTriggerRejectsControllerAsTarget() {
        Permanent sacrifice = addCreatureReady(player1, new ColossodonYearling());

        castQarsiSadist();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Qarsi Sadist can exploit itself and still drain its opponent")
    void exploitingItselfDrainsOpponent() {
        castQarsiSadist();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Qarsi Sadist"));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Qarsi Sadist");
        harness.assertNotOnBattlefield(player1, "Qarsi Sadist");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Removing Qarsi Sadist before exploit resolves allows sacrifice but does not drain")
    void removedBeforeExploitDoesNotDrain() {
        Permanent sacrifice = addCreatureReady(player1, new ColossodonYearling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new QarsiSadist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new DeathWind()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, 3, harness.getPermanentId(player1, "Qarsi Sadist"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Qarsi Sadist");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Colossodon Yearling");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Qarsi Sadist after it exploits does not stop its drain trigger")
    void drainResolvesAfterSourceIsRemoved() {
        Permanent sacrifice = addCreatureReady(player1, new ColossodonYearling());
        harness.setHand(player2, List.of(new DeathWind()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castQarsiSadist();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, 3, harness.getPermanentId(player1, "Qarsi Sadist"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Qarsi Sadist");
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void castQarsiSadist() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new QarsiSadist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
