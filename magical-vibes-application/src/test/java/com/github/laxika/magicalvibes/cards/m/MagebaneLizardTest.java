package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagebaneLizard.class, GrizzlyBears.class, HolyDay.class})
class MagebaneLizardTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToNoncreatureSpellsCastThisTurn() {
        harness.addToBattlefield(player1, new MagebaneLizard());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        resolveAllTriggers();
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void countsOnlyNoncreatureSpells() {
        harness.addToBattlefield(player1, new MagebaneLizard());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new HolyDay()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void alsoTriggersWhenItsControllerCastsANoncreatureSpell() {
        harness.addToBattlefield(player1, new MagebaneLizard());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void countsAdditionalSpellsCastInResponseWhenEachTriggerResolves() {
        harness.addToBattlefield(player1, new MagebaneLizard());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(4);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void countsSpellsCastBeforeLizardEnteredTheBattlefield() {
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        harness.addToBattlefield(player1, new MagebaneLizard());
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void tracksEachPlayersNoncreatureSpellsSeparately() {
        harness.addToBattlefield(player1, new MagebaneLizard());
        prepareMainPhase(player2);
        harness.setHand(player1, List.of(new HolyDay()));
        harness.setHand(player2, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);
        int player1Life = gd.playerLifeTotals.get(player1.getId());
        int player2Life = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        harness.castInstant(player1, 0);
        resolveAllTriggers();
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life - 3);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
