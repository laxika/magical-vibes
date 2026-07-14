package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GwyllionHedgeMageTest extends BaseCardTest {

    // ===== Plains gate: may create a Kithkin Soldier token =====

    @Test
    @DisplayName("With two Plains, ETB may create a 1/1 white Kithkin Soldier token")
    void plainsGateCreatesToken() {
        addLands(player1, 2, 0);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell -> token ETB on stack
        harness.passBothPriorities(); // resolve ConditionalEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countKithkinSoldierTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the Plains trigger creates no token")
    void plainsGateDeclinedCreatesNoToken() {
        addLands(player1, 2, 0);
        castGwyllion();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countKithkinSoldierTokens(player1)).isZero();
    }

    @Test
    @DisplayName("With only one Plains the token trigger does not fire")
    void onePlainsDoesNotTrigger() {
        addLands(player1, 1, 0);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countKithkinSoldierTokens(player1)).isZero();
    }

    // ===== Swamp gate: may put a -1/-1 counter on target creature =====

    @Test
    @DisplayName("With two Swamps, ETB may put a -1/-1 counter on target creature")
    void swampGatePutsMinusCounter() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell -> trigger-time target prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve ConditionalEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the Swamp trigger leaves the target creature unchanged")
    void swampGateDeclinedLeavesTargetUnchanged() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("With only one Swamp the -1/-1 trigger does not fire (no target prompt)")
    void oneSwampDoesNotTrigger() {
        addLands(player1, 0, 1);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Neither gate met =====

    @Test
    @DisplayName("With no Plains or Swamps, neither ability triggers")
    void neitherGateTriggers() {
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gwyllion Hedge-Mage"));
    }

    // ===== Both gates met =====

    @Test
    @DisplayName("With two Plains and two Swamps, both abilities may resolve")
    void bothGatesResolve() {
        addLands(player1, 2, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell -> target prompt for -1/-1
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve bundled ETB -> first may prompt (token)
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true); // second may prompt (-1/-1 counter)

        assertThat(countKithkinSoldierTokens(player1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    // ===== Helpers =====

    private void castGwyllion() {
        harness.setHand(player1, List.of(new GwyllionHedgeMage()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void addLands(Player player, int plains, int swamps) {
        for (int i = 0; i < plains; i++) {
            harness.addToBattlefield(player, new Plains());
        }
        for (int i = 0; i < swamps; i++) {
            harness.addToBattlefield(player, new Swamp());
        }
    }

    private Permanent addBears(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private int countKithkinSoldierTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .count();
    }
}
