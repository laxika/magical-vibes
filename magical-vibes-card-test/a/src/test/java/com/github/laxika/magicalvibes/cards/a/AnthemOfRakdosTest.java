package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Demonfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnthemOfRakdos.class, AssaultZeppelid.class, Demonfire.class})
class AnthemOfRakdosTest extends BaseCardTest {

    @Test
    void boostsAttackingCreatureAndDamagesController() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void hellbentDoublesCombatDamageAndAnthemDamage() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        addCreatureReady(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    void hellbentDoublesDamageFromControllerSpell() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void hellbentDoublesDamageToPermanent() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        Permanent target = addCreatureReady(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void eachAttackerGetsItsOwnBoostAndDamageTrigger() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        Permanent first = addCreatureReady(player1, new AssaultZeppelid());
        Permanent second = addCreatureReady(player1, new AssaultZeppelid());
        Permanent nonattacker = addCreatureReady(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void controllerSpellDamageIsNotDoubledWithACardRemainingInHand() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        harness.setHand(player1, List.of(new Demonfire(), new AssaultZeppelid()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void hellbentDoesNotDoubleOpponentsSpellDamage() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Demonfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, 2, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void hellbentChecksHandWhenAttackTriggerDealsDamage() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.setLife(player1, 20);

        declareAttackers(List.of(1));
        harness.setHand(player1, List.of());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void gainingACardBeforeAttackTriggerResolvesDisablesHellbent() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        declareAttackers(List.of(1));
        harness.setHand(player1, List.of(new Demonfire()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void attackBoostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AnthemOfRakdos());
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }
}
