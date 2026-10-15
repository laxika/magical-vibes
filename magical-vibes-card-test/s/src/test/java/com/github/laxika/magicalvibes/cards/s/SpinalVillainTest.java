package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinalVillain.class, AirElemental.class, GrayOgre.class, ControlMagic.class, Unsummon.class})
class SpinalVillainTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target blue creature")
    void destroysTargetBlueCreature() {
        Permanent villain = addCreatureReady(player1, new SpinalVillain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(villain.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Can destroy a blue creature controlled by its controller")
    void destroysOwnBlueCreature() {
        Permanent villain = addCreatureReady(player1, new SpinalVillain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(villain.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a nonblue creature")
    void cannotTargetNonblueCreature() {
        Permanent villain = addCreatureReady(player1, new SpinalVillain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayOgre());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(villain.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a blue noncreature permanent")
    void cannotTargetBlueNoncreature() {
        Permanent villain = addCreatureReady(player1, new SpinalVillain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ControlMagic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(villain.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A regeneration shield can replace the destruction")
    void respectsRegenerationShield() {
        Permanent villain = addCreatureReady(player1, new SpinalVillain());
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(villain.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new SpinalVillain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(villain.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent villain = addCreatureReady(player1, new SpinalVillain());
        villain.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Ability still destroys its target after Spinal Villain leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent villain = addCreatureReady(player1, new SpinalVillain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, villain.getId());
        harness.assertInHand(player1, "Spinal Villain");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not destroy a target returned to hand in response")
    void targetLeavingBattlefieldMakesAbilityIllegal() {
        Permanent villain = addCreatureReady(player1, new SpinalVillain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(villain.isTapped()).isTrue();
        harness.assertInHand(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        assertThat(gd.stack).isEmpty();
    }
}
