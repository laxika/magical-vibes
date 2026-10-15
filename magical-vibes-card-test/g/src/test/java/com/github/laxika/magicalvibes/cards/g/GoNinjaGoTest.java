package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CounterType;
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

    @Test
    void rejectsAThirdModeNotPresentInOracleText() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThatThrownBy(() -> castGoNinjaGo(2, List.of(ownCreature.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeDealsNoDamageWithoutControlledCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGoNinjaGo(1, List.of(target.getId()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void bothModesCalculatePowerAfterBlinkRemovesCounters() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        ownCreature.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castGoNinjaGo(new int[]{0, 1}, List.of(ownCreature.getId(), target.getId()));

        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(ownCreature.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void blinkReturnsBorrowedCreatureToItsOwnerBeforeCalculatingDamage() {
        GrizzlyBears borrowedCard = new GrizzlyBears();
        borrowedCard.setOwnerId(player2.getId());
        Permanent borrowedCreature = harness.addToBattlefieldAndReturn(player1, borrowedCard);
        gd.stolenCreatures.put(borrowedCreature.getId(), player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castGoNinjaGo(new int[]{0, 1}, List.of(borrowedCreature.getId(), target.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(borrowedCreature.getId());
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void losingBlinkTargetDoesNotStopDamageMode() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castGoNinjaGo(new int[]{0, 1}, List.of(ownCreature.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerGraveyards.get(player1.getId()).add(ownCreature.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void losingDamageTargetDoesNotStopBlinkMode() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castGoNinjaGo(new int[]{0, 1}, List.of(ownCreature.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(ownCreature.getId());
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void rejectsRepeatingDamageThroughInventedCombinedMode() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThatThrownBy(() -> castGoNinjaGo(new int[]{1, 2},
                List.of(firstOpponentCreature.getId(), ownCreature.getId(), secondOpponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
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
