package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KuroPitlordTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B}{B}{B}{B} at upkeep keeps Kuro on the battlefield")
    void payingKeepsKuro() {
        harness.addToBattlefield(player1, new KuroPitlord());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Kuro, Pitlord")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Kuro")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new KuroPitlord());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Kuro, Pitlord")).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices Kuro")
    void notEnoughManaSacrifices() {
        harness.addToBattlefield(player1, new KuroPitlord());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Kuro, Pitlord")).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new KuroPitlord());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Kuro, Pitlord")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Pay 1 life: target creature gets -1/-1 until end of turn")
    void payLifeShrinksTargetCreature() {
        harness.addToBattlefield(player1, new KuroPitlord());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations stack and kill the target")
    void repeatedActivationsKillTarget() {
        harness.addToBattlefield(player1, new KuroPitlord());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.setLife(player1, 20);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, bears.getId());
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new KuroPitlord());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new KuroPitlord());
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.f.Forest());
        Permanent land = findPermanent(player2, "Forest");
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
