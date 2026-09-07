package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoNinjaGo.class, GrizzlyBears.class, AirElemental.class})
class GoNinjaGoTest extends BaseCardTest {

    @Test
    @DisplayName("The blink mode exiles and immediately returns a creature you control")
    void blinkModeReturnsOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID originalId = creature.getId();
        castGoNinjaGo(0, List.of(originalId));

        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("The damage mode uses the greatest power among creatures you control")
    void damageModeUsesGreatestControlledPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castGoNinjaGo(1, List.of(target.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Choosing both modes blinks your creature and damages an opponent's creature")
    void choosesBothModes() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        UUID originalId = ownCreature.getId();
        castGoNinjaGo(new int[]{0, 1}, List.of(originalId, target.getId()));

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isNotEqualTo(originalId);
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("The blink mode cannot target an opponent's creature")
    void blinkModeRequiresCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castGoNinjaGo(0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The damage mode cannot target your own creature")
    void damageModeRequiresOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> castGoNinjaGo(1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    private void castGoNinjaGo(int mode, List<UUID> targetIds) {
        castGoNinjaGo(new int[]{mode}, targetIds);
    }

    private void castGoNinjaGo(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new GoNinjaGo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
    }
}
