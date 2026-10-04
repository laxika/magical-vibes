package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImperviousGreatwurm.class, Terminate.class})
class ImperviousGreatwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Indestructible lets Impervious Greatwurm survive lethal damage")
    void indestructibleSurvivesLethalDamage() {
        Permanent greatwurm = addCreatureReady(player1, new ImperviousGreatwurm());
        greatwurm.setMarkedDamage(16);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(greatwurm);
    }

    @Test
    @DisplayName("Convoke taps creatures to pay for Impervious Greatwurm")
    void convokeTapsCreaturesToPayForIt() {
        Permanent firstHelper = addCreatureReady(player1, new ImperviousGreatwurm());
        Permanent secondHelper = addCreatureReady(player1, new ImperviousGreatwurm());
        Permanent thirdHelper = addCreatureReady(player1, new ImperviousGreatwurm());
        harness.setHand(player1, List.of(new ImperviousGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstHelper.getId(), secondHelper.getId(), thirdHelper.getId()));

        assertThat(firstHelper.isTapped()).isTrue();
        assertThat(secondHelper.isTapped()).isTrue();
        assertThat(thirdHelper.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Impervious Greatwurm")).isEqualTo(4);
    }

    @Test
    @DisplayName("Summoning-sick green creatures can pay all three green symbols with convoke")
    void summoningSickCreaturesPayColoredCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ImperviousGreatwurm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ImperviousGreatwurm());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new ImperviousGreatwurm());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setHand(player1, List.of(new ImperviousGreatwurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Impervious Greatwurm")).isEqualTo(4);
    }

    @Test
    @DisplayName("Impervious Greatwurm can be cast without convoke")
    void castsWithoutConvoke() {
        harness.castFromHand(player1, new ImperviousGreatwurm(), "{7}{G}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Impervious Greatwurm");
    }

    @Test
    @DisplayName("Tapped creatures cannot convoke Impervious Greatwurm")
    void tappedCreatureCannotConvoke() {
        Permanent helper = addCreatureReady(player1, new ImperviousGreatwurm());
        helper.tap();
        harness.setHand(player1, List.of(new ImperviousGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(helper.getId()))).isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Impervious Greatwurm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opposing creatures cannot convoke Impervious Greatwurm")
    void opposingCreatureCannotConvoke() {
        Permanent helper = addCreatureReady(player2, new ImperviousGreatwurm());
        harness.setHand(player1, List.of(new ImperviousGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(helper.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        harness.assertInHand(player1, "Impervious Greatwurm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature cannot convoke twice for one spell")
    void duplicateConvokeCreatureIsRejected() {
        Permanent helper = addCreatureReady(player1, new ImperviousGreatwurm());
        harness.setHand(player1, List.of(new ImperviousGreatwurm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(helper.getId(), helper.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        harness.assertInHand(player1, "Impervious Greatwurm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Indestructible prevents Terminate even though regeneration is forbidden")
    void survivesTerminate() {
        Permanent greatwurm = addCreatureReady(player2, new ImperviousGreatwurm());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, greatwurm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(greatwurm);
        harness.assertNotInGraveyard(player2, "Impervious Greatwurm");
        harness.assertInGraveyard(player1, "Terminate");
    }
}
