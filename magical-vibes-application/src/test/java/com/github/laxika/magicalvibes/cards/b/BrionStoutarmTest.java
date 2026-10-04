package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TreefolkHarbinger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrionStoutarm.class, GrizzlyBears.class, GarrukWildspeaker.class, CribSwap.class,
        TreefolkHarbinger.class})
class BrionStoutarmTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to sacrificed creature's power to target player")
    void dealsSacrificedPowerToPlayer() {
        addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears()); // 2/2
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's boosted (effective) power")
    void usesBoostedPower() {
        addReadyBrion(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power becomes 3
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Taps Brion and consumes the red mana as part of the cost")
    void tapsAndConsumesMana() {
        Permanent brion = addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(brion.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate when Brion is the only creature (excludeSelf)")
    void cannotSacrificeItself() {
        addReadyBrion(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature as the damage target")
    void rejectsCreatureTarget() {
        addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID creatureTarget = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureTarget))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsLifeFromAbilityDamage() {
        addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    void canTargetItsControllerAndGainLifeFromThatDamage() {
        addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void damagesPlaneswalkerAndGainsLife() {
        addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, garruk.getId());
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
    }

    @Test
    void chosenSacrificeUsesPowerAtPayment() {
        addReadyBrion(player1);
        Permanent unchosen = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 14);
    }

    @Test
    void resolvesWithLifelinkAfterBrionIsExiled() {
        Permanent brion = addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new CribSwap()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, brion.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Brion Stoutarm");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 12);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BrionStoutarm());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent brion = addReadyBrion(player1);
        brion.tap();
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        addReadyBrion(player1);
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void sacrificingZeroPowerCreatureDealsNoDamageAndGainsNoLife() {
        addReadyBrion(player1);
        harness.addToBattlefield(player1, new TreefolkHarbinger());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Treefolk Harbinger");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyBrion(Player player) {
        return addCreatureReady(player, new BrionStoutarm());
    }
}
