package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.cards.s.StingingBarrier;
import com.github.laxika.magicalvibes.cards.s.Sizzle;
import com.github.laxika.magicalvibes.cards.t.ThermalGlider;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeneralsRegalia.class, StingingBarrier.class, HengeGuardian.class,
        Sizzle.class, ThermalGlider.class, Vendetta.class})
class GeneralsRegaliaTest extends BaseCardTest {

    @Test
    void redirectsNextDamageFromChosenSourceToControlledCreature() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, barrier.getId());

        harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);

        barrier.untap();
        harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void onlyRedirectsDamageFromTheChosenSource() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent otherSource = addCreatureReady(player1, new StingingBarrier());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherSource.getId());

        harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotRedirectDamageToAControlledCreature() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent damagedCreature = addCreatureReady(player1, new HengeGuardian());
        Permanent redirectCreature = addCreatureReady(player1, new HengeGuardian());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, indexOf(player1, regalia), null, redirectCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, barrier.getId());

        harness.activateAbility(player1, indexOf(player1, barrier), null, damagedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(damagedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(redirectCreature.getMarkedDamage()).isZero();
    }

    @Test
    void redirectsCombatDamageFromChosenSource() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent redirectCreature = addCreatureReady(player1, new HengeGuardian());
        Permanent attacker = addCreatureReady(player2, new HengeGuardian());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, regalia), null, redirectCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void onlyTargetsCreatureYouControl() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent opponentCreature = addCreatureReady(player2, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, regalia), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void redirectsDamageFromASpellChosenOnTheStack() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        Sizzle sizzle = new Sizzle();
        harness.setHand(player2, List.of(sizzle));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int lifeBefore = gd.getLife(player1.getId());

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castSorcery(player2, 0);
            harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, sizzle.getId());
            harness.passBothPriorities();
        });

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void protectionPreventsRedirectedDamageFromARedSpell() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent creature = addCreatureReady(player1, new ThermalGlider());
        Sizzle sizzle = new Sizzle();
        harness.setHand(player2, List.of(sizzle));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int lifeBefore = gd.getLife(player1.getId());

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castSorcery(player2, 0);
            harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, sizzle.getId());
            harness.passBothPriorities();
        });

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Thermal Glider");
    }

    @Test
    void canChooseDepartedSourceOfAnAbilityStillOnTheStack() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.withAutoStop(gd.currentStep, () -> {
            harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
            harness.castAndResolveInstant(player1, 0, barrier.getId());
            harness.assertInGraveyard(player1, "Stinging Barrier");
            int lifeBefore = gd.getLife(player1.getId());

            harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
            harness.passBothPriorities();
            PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                    PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validPermanentIds()).contains(barrier.getId());
            harness.handlePermanentChosen(player1, barrier.getId());
            harness.passBothPriorities();

            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
            assertThat(creature.getMarkedDamage()).isEqualTo(1);
        });
    }

    @Test
    void doesNotRedirectWhenDestinationLeavesTheBattlefield() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, barrier.getId());

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Henge Guardian");
        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void unusedRedirectionExpiresAtEndOfTurn() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, barrier.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
