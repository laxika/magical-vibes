package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfVituGhazi.class, ElvesOfDeepShadow.class, CourierHawk.class})
class GuardianOfVituGhaziTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay Guardian of Vitu-Ghazi's cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        harness.setHand(player1, List.of(new GuardianOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Guardian of Vitu-Ghazi")).isEqualTo(1);
    }

    @Test
    @DisplayName("Convoke can pay Guardian of Vitu-Ghazi's colored costs")
    void convokePaysColoredCosts() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        harness.setHand(player1, List.of(new GuardianOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Guardian of Vitu-Ghazi")).isEqualTo(1);
    }

    @Test
    @DisplayName("Vigilance keeps Guardian of Vitu-Ghazi untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfVituGhazi());

        declareAttackers(List.of(0));

        assertThat(guardian.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Guardian can be cast without using convoke")
    void castsWithoutConvoke() {
        harness.setHand(player1, List.of(new GuardianOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guardian of Vitu-Ghazi");
    }

    @Test
    @DisplayName("Summoning-sick creatures can convoke without activating their mana abilities")
    void summoningSickCreaturesCanConvoke() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        elf.setSummoningSick(true);
        hawk.setSummoningSick(true);
        harness.setHand(player1, List.of(new GuardianOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(elf.getId(), hawk.getId()));
        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(hawk.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Guardian of Vitu-Ghazi");
    }

    @Test
    @DisplayName("A tapped creature cannot convoke Guardian")
    void rejectsTappedConvokeCreature() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        elf.tap();
        harness.setHand(player1, List.of(new GuardianOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(elf.getId()))).isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Guardian of Vitu-Ghazi");
        harness.assertNotOnBattlefield(player1, "Guardian of Vitu-Ghazi");
    }

    @Test
    @DisplayName("An opponent's creature cannot convoke Guardian")
    void rejectsOpponentsConvokeCreature() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new ElvesOfDeepShadow());
        harness.setHand(player1, List.of(new GuardianOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(elf.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(elf.isTapped()).isFalse();
        harness.assertInHand(player1, "Guardian of Vitu-Ghazi");
    }

    @Test
    @DisplayName("A green creature cannot convoke Guardian's white mana requirement")
    void rejectsWrongColorForWhiteCost() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        harness.setHand(player1, List.of(new GuardianOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(elf.getId()))).isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Guardian of Vitu-Ghazi");
        harness.assertNotOnBattlefield(player1, "Guardian of Vitu-Ghazi");
    }
}
