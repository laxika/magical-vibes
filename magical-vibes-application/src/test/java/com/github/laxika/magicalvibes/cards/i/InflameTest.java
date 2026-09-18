package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Inflame.class, BarbedLightning.class, DarksteelColossus.class, DarksteelGargoyle.class, DarksteelIngot.class})
class InflameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature dealt damage this turn")
    void damagesOnlyCreaturesDealtDamageThisTurn() {
        Permanent damagedCreature = harness.addToBattlefieldAndReturn(player2, new DarksteelColossus());
        Permanent undamagedCreature = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new BarbedLightning(), new Inflame()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(damagedCreature.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);

        assertThat(damagedCreature.getMarkedDamage()).isEqualTo(5);
        assertThat(undamagedCreature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not damage creatures that have not been dealt damage this turn")
    void doesNothingWithoutPreviouslyDamagedCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new Inflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage noncreature permanents")
    void ignoresNoncreaturePermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new Inflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(artifact.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Includes creatures dealt combat damage this turn")
    void damagesCreaturesDealtCombatDamageThisTurn() {
        Permanent attacker = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent damagedCreature = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.setHand(player1, List.of(new Inflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getMarkedDamage()).isEqualTo(5);
        assertThat(damagedCreature.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not affect creatures damaged during a previous turn")
    void ignoresDamageFromPreviousTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new BarbedLightning(), new Inflame()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(creature.getMarkedDamage()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.getMarkedDamage()).isZero();
    }
}
