package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeadeyeNavigatorTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeadeyeNavigator()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    @Test
    @DisplayName("Soulbond ETB pairs Deadeye with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent navigator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Deadeye Navigator"))
                .findFirst().orElseThrow();

        assertThat(navigator.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(navigator.getId());
    }

    @Test
    @DisplayName("While paired, Deadeye can flicker itself")
    void pairedNavigatorCanFlickerSelf() {
        castAndPairWithBears();
        Permanent navigator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Deadeye Navigator"))
                .findFirst().orElseThrow();
        UUID oldId = navigator.getId();

        harness.addMana(player1, ManaColor.BLUE, 2);
        int navIndex = gd.playerBattlefields.get(player1.getId()).indexOf(navigator);
        harness.activateAbility(player1, navIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Deadeye Navigator"))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(oldId);
        // Exile breaks the pair immediately.
        assertThat(returned.getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("While paired, the partner can flicker itself")
    void pairedPartnerCanFlickerSelf() {
        Permanent bears = castAndPairWithBears();
        UUID oldBearsId = bears.getId();

        harness.addMana(player1, ManaColor.BLUE, 2);
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        harness.activateAbility(player1, bearsIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent returnedBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(returnedBears.getId()).isNotEqualTo(oldBearsId);
        assertThat(returnedBears.getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Unpaired Deadeye does not have the flicker ability")
    void unpairedCannotFlicker() {
        harness.addToBattlefield(player1, new DeadeyeNavigator());
        Permanent navigator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Deadeye Navigator"))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.BLUE, 2);

        int navIndex = gd.playerBattlefields.get(player1.getId()).indexOf(navigator);
        assertThatThrownBy(() -> harness.activateAbility(player1, navIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired")
    void decliningLeavesUnpaired() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeadeyeNavigator()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent navigator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Deadeye Navigator"))
                .findFirst().orElseThrow();
        assertThat(navigator.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }
}
