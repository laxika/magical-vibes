package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.k.Kindle;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.w.WingedSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClotSliver.class, WingedSliver.class, LowlandGiant.class, AmoeboidChangeling.class, Kindle.class})
class ClotSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain the regeneration ability")
    void grantsAbilityToAllSlivers() {
        Permanent clotSliver = addCreatureReady(player1, new ClotSliver());
        Permanent ownSliver = addCreatureReady(player1, new WingedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new WingedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, clotSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new ClotSliver());
        Permanent nonSliver = addCreatureReady(player1, new LowlandGiant());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
    }

    @Test
    @DisplayName("Activating the granted ability grants a regeneration shield to that Sliver")
    void grantsRegenerationShield() {
        addCreatureReady(player1, new ClotSliver());
        Permanent otherSliver = addCreatureReady(player1, new WingedSliver());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(otherSliver.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @CardUsed(AmoeboidChangeling.class)
    @DisplayName("A Sliver that loses all creature types no longer has the ability")
    void losingSliverTypeRemovesAbility() {
        Permanent clotSliver = addCreatureReady(player1, new ClotSliver());
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());

        assertThat(gs.getEffectiveActivatedAbilities(gd, clotSliver)).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 1, null, clotSliver.getId());
        harness.passBothPriorities();

        assertThat(gs.getEffectiveActivatedAbilities(gd, clotSliver)).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Clot Sliver can regenerate itself")
    void regeneratesItselfWhileTappedAndSummoningSick() {
        Permanent clotSliver = harness.addToBattlefieldAndReturn(player1, new ClotSliver());
        clotSliver.setSummoningSick(true);
        clotSliver.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(clotSliver.getRegenerationShield()).isEqualTo(1);
        assertThat(clotSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opposing Sliver uses its controller's mana and regenerates from lethal damage")
    void opposingSliverRegeneratesFromLethalDamage() {
        Permanent clotSliver = addCreatureReady(player1, new ClotSliver());
        Permanent opposingSliver = addCreatureReady(player2, new WingedSliver());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(opposingSliver.getRegenerationShield()).isEqualTo(1);
        assertThat(clotSliver.getRegenerationShield()).isZero();
        assertThat(opposingSliver.isTapped()).isFalse();

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, opposingSliver.getId());

        harness.assertOnBattlefield(player2, "Winged Sliver");
        assertThat(opposingSliver.getRegenerationShield()).isZero();
        assertThat(opposingSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A granted ability already on the stack resolves after Clot Sliver leaves")
    void pendingAbilityResolvesAfterGrantingSliverDies() {
        Permanent clotSliver = addCreatureReady(player1, new ClotSliver());
        Permanent otherSliver = addCreatureReady(player1, new WingedSliver());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);

        harness.setHand(player2, List.of(new Kindle()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, clotSliver.getId());

        harness.assertInGraveyard(player1, "Clot Sliver");
        assertThat(gs.getEffectiveActivatedAbilities(gd, otherSliver)).isEmpty();
        resolveAllTriggers();
        assertThat(otherSliver.getRegenerationShield()).isEqualTo(1);
    }
}
