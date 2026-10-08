package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindbornMuse.class, GrizzlyBears.class, ChandraNalaar.class})
class WindbornMuseTest extends BaseCardTest {

    @Test
    @DisplayName("Windborn Muse's attack tax disappears when it loses all abilities")
    void attackTaxDisappearsWhenMuseLosesAllAbilities() {
        Permanent muse = harness.addToBattlefieldAndReturn(player2, new WindbornMuse());
        muse.setLosesAllAbilitiesUntilEndOfTurn(true);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackersAndPrepareBlockers(List.of(0))).doesNotThrowAnyException();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The attacker pays two generic mana to attack the Muse's controller")
    void paysTwoGenericManaPerAttacker() {
        harness.addToBattlefield(player2, new WindbornMuse());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An attack without payment is rejected without tapping the attacker")
    void cannotAttackWithoutPayment() {
        harness.addToBattlefield(player2, new WindbornMuse());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each declared attacker costs two mana")
    void paysForEachDeclaredAttacker() {
        harness.addToBattlefield(player2, new WindbornMuse());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An unaffordable attack declaration neither spends mana nor taps creatures")
    void insufficientManaDoesNotPartiallyPay() {
        harness.addToBattlefield(player2, new WindbornMuse());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(first.isAttacking()).isFalse();
        assertThat(second.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A creature that stays out of combat does not increase the tax")
    void nonattackerDoesNotIncreaseTax() {
        harness.addToBattlefield(player2, new WindbornMuse());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(nonattacker.isTapped()).isFalse();
        assertThat(nonattacker.isAttacking()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two Muses require four mana for one attacker")
    void multipleMusesAddTheirTaxes() {
        harness.addToBattlefield(player2, new WindbornMuse());
        harness.addToBattlefield(player2, new WindbornMuse());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(attacker.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The Muse does not tax its controller's attacks")
    void controllerCanAttackWithoutPayment() {
        harness.addToBattlefield(player1, new WindbornMuse());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Choosing no attackers requires no payment")
    void emptyDeclarationRequiresNoPayment() {
        harness.addToBattlefield(player2, new WindbornMuse());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The tax stops when the Muse leaves the battlefield")
    void removedMuseDoesNotTaxAttacks() {
        Permanent muse = harness.addToBattlefieldAndReturn(player2, new WindbornMuse());
        addCreatureReady(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(muse);
        gd.playerGraveyards.get(player2.getId()).add(muse.getCard());

        declareAttackers(List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Attacking the controller's planeswalker does not require payment")
    void planeswalkerAttackIsNotTaxed() {
        harness.addToBattlefield(player2, new WindbornMuse());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new ChandraNalaar());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAtTargets(List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A mixed attack pays only for the creature attacking the player")
    void mixedAttackTaxesOnlyPlayerTarget() {
        harness.addToBattlefield(player2, new WindbornMuse());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new ChandraNalaar());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackersAtTargets(List.of(0, 1), Map.of(1, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void declareAttackersAtTargets(List<Integer> attackerIndices, Map<Integer, UUID> targets) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, attackerIndices, targets);
        });
        prepareDeclareBlockers();
    }
}
