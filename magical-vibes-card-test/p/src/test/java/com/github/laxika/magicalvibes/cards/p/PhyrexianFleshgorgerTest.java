package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.m.MeticulousExcavation;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianFleshgorger.class, Shock.class, Disfigure.class, MeticulousExcavation.class})
class PhyrexianFleshgorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype ward costs life equal to the prototype power")
    void prototypeWardUsesPrototypePower() {
        Permanent fleshgorger = castPrototype();
        harness.setLife(player2, 20);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
    }

    @Test
    @DisplayName("Normal ward costs life equal to the full power")
    void normalWardUsesNormalPower() {
        Permanent fleshgorger = castNormally();
        harness.setLife(player2, 20);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
    }

    @Test
    @DisplayName("Declining ward counters the targeted spell")
    void decliningWardCountersSpell() {
        Permanent fleshgorger = castPrototype();
        harness.setLife(player2, 20);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward counters the spell immediately when its controller cannot pay")
    void wardCountersWhenControllerCannotPay() {
        Permanent fleshgorger = castPrototype();
        harness.setLife(player2, 2);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent fleshgorger = castPrototype();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, fleshgorger.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Disfigure");
        assertThat(gqs.getEffectivePower(gd, fleshgorger)).isEqualTo(1);
    }

    @Test
    void wardUsesPowerAtResolutionAfterOwnSpellReducesIt() {
        Permanent fleshgorger = castPrototype();
        harness.setLife(player2, 20);
        castShockAtFleshgorger(fleshgorger);
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, fleshgorger.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Phyrexian Fleshgorger");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void wardUsesLastKnownReducedPowerAfterSourceReturnsToHand() {
        Permanent fleshgorger = castPrototype();
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, fleshgorger.getId());
        harness.addToBattlefield(player1, new MeticulousExcavation());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, fleshgorger.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, fleshgorger.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Phyrexian Fleshgorger");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotOnBattlefield(player1, "Phyrexian Fleshgorger");
    }

    @Test
    void normalRecastAfterReturningPrototypeToHandRestoresFullWardCost() {
        Permanent prototype = castPrototype();
        harness.addToBattlefield(player1, new MeticulousExcavation());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, prototype.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Phyrexian Fleshgorger");
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent fleshgorger = findPermanent(player1, "Phyrexian Fleshgorger");
        harness.setLife(player2, 20);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
    }

    @Test
    void prototypeDealsThreeCombatDamageAndGainsThreeLife() {
        Permanent fleshgorger = castPrototype();
        fleshgorger.setSummoningSick(false);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }

    @Test
    void normalCastDealsSevenCombatDamageAndGainsSevenLife() {
        Permanent fleshgorger = castNormally();
        fleshgorger.setSummoningSick(false);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 13);
    }

    @Test
    void prototypeCannotBeBlockedByOnlyOneCreature() {
        Permanent fleshgorger = castPrototype();
        fleshgorger.setSummoningSick(false);
        addCreatureReady(player2, new PhyrexianFleshgorger());
        addCreatureReady(player2, new PhyrexianFleshgorger());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void prototypeCanBeBlockedByTwoCreatures() {
        Permanent fleshgorger = castPrototype();
        fleshgorger.setSummoningSick(false);
        Permanent firstBlocker = addCreatureReady(player2, new PhyrexianFleshgorger());
        Permanent secondBlocker = addCreatureReady(player2, new PhyrexianFleshgorger());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    private Permanent castNormally() {
        harness.castFromHand(player1, new PhyrexianFleshgorger(), "{7}");
        harness.passBothPriorities();
        return findPermanent(player1, "Phyrexian Fleshgorger");
    }

    private Permanent castPrototype() {
        harness.setHand(player1, List.of(new PhyrexianFleshgorger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent fleshgorger = findPermanent(player1, "Phyrexian Fleshgorger");
        assertThat(gqs.getEffectivePower(gd, fleshgorger)).isEqualTo(3);
        return fleshgorger;
    }

    private void castShockAtFleshgorger(Permanent fleshgorger) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, fleshgorger.getId());
    }
}
