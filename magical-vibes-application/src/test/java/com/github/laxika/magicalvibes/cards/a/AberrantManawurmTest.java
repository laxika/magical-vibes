package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AberrantManawurm.class, Shock.class, Divination.class, Hurricane.class,
        GrizzlyBears.class, LightningBolt.class})
class AberrantManawurmTest extends BaseCardTest {

    private Permanent addManawurm(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AberrantManawurm());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    

    @Test
    @DisplayName("Casting a one-mana instant gives +1/+0 until end of turn")
    void castingOneManaInstantGivesPlusOne() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(manawurm.getPowerModifier()).isEqualTo(1);
        assertThat(manawurm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mana spent from a creature source counts once, not double")
    void castingWithCreatureManaCountsOnce() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);

        harness.addCreatureMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(manawurm.getPowerModifier()).isEqualTo(1);
        assertThat(manawurm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a three-mana sorcery gives +3/+0 until end of turn")
    void castingThreeManaSorceryGivesPlusThree() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player1, List.of(new Divination()));
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(manawurm.getPowerModifier()).isEqualTo(3);
        assertThat(manawurm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting an X spell gives boost equal to total mana spent, not just X")
    void castingXSpellUsesTotalManaSpent() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(manawurm.getPowerModifier()).isEqualTo(4);
        assertThat(manawurm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a creature spell does not boost Aberrant Manawurm")
    void castingCreatureDoesNotBoost() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(manawurm.getPowerModifier()).isEqualTo(0);
        assertThat(manawurm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple instant casts stack the power boost")
    void multipleCastsStackBoost() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(manawurm.getPowerModifier()).isEqualTo(1);

        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(manawurm.getPowerModifier()).isEqualTo(2);
        assertThat(manawurm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's instant does not boost Aberrant Manawurm")
    void opponentInstantDoesNotBoost() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(manawurm.getPowerModifier()).isZero();
        assertThat(manawurm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Power boost expires at end of turn")
    void powerBoostExpiresAtEndOfTurn() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(manawurm.getPowerModifier()).isEqualTo(1);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(manawurm.getPowerModifier()).isZero();
        assertThat(manawurm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An X spell cast with X zero still counts its colored mana cost")
    void zeroXStillCountsColoredMana() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Hurricane()));

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(manawurm.getPowerModifier()).isEqualTo(1);
        assertThat(manawurm.getToughnessModifier()).isZero();
    }

}
