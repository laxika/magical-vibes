package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.b.BorealShelf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianSoulgorger.class, BorealCentaur.class, BorealShelf.class})
class PhyrexianSoulgorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Insufficient creatures cannot be sacrificed as a partial cumulative upkeep payment")
    void insufficientCreaturesAreNotPartiallySacrificed() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());
        soulgorger.setCounterCount(CounterType.AGE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(centaur).doesNotContain(soulgorger);
        harness.assertInGraveyard(player1, "Phyrexian Soulgorger");
        harness.assertNotInGraveyard(player1, "Boreal Centaur");
    }

    @Test
    @DisplayName("Opponent's creatures cannot pay cumulative upkeep")
    void opponentsCreaturesCannotPay() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());
        Permanent opponentCentaur = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(soulgorger.getId(), centaur.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(centaur.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(soulgorger).doesNotContain(centaur);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCentaur);
    }

    @Test
    @DisplayName("A two-creature upkeep payment may include Phyrexian Soulgorger itself")
    void twoCreaturePaymentCanIncludeSource() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());
        soulgorger.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(soulgorger, centaur);
        harness.assertInGraveyard(player1, "Phyrexian Soulgorger");
        harness.assertInGraveyard(player1, "Boreal Centaur");
    }

    @Test
    @DisplayName("Paying cumulative upkeep sacrifices a creature and keeps Phyrexian Soulgorger")
    void paysCumulativeUpkeep() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(soulgorger.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultiplePermanentsChosen(player1, List.of(centaur.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(soulgorger).doesNotContain(centaur);
        harness.assertInGraveyard(player1, "Boreal Centaur");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Phyrexian Soulgorger")
    void declineSacrifices() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(soulgorger);
        harness.assertInGraveyard(player1, "Phyrexian Soulgorger");
    }

    @Test
    @DisplayName("Phyrexian Soulgorger can be sacrificed to pay its own cumulative upkeep")
    void canSacrificeItselfToPay() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(soulgorger);
        harness.assertInGraveyard(player1, "Phyrexian Soulgorger");
    }

    @Test
    @DisplayName("Second upkeep requires sacrificing two creatures")
    void secondUpkeepSacrificesTwoCreatures() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());
        Permanent firstCentaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCentaur.getId()));

        assertThat(soulgorger.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstCentaur);

        Permanent secondCentaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());
        Permanent thirdCentaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(soulgorger.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                soulgorger.getId(), secondCentaur.getId(), thirdCentaur.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(secondCentaur.getId(), thirdCentaur.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(soulgorger)
                .doesNotContain(secondCentaur, thirdCentaur);
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during Phyrexian Soulgorger's controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(soulgorger.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(soulgorger);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cumulative upkeep can sacrifice only creatures")
    void onlyCreaturesCanBeSacrificed() {
        Permanent soulgorger = harness.addToBattlefieldAndReturn(player1, new PhyrexianSoulgorger());
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());
        Permanent shelf = harness.addToBattlefieldAndReturn(player1, new BorealShelf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(soulgorger.getId(), centaur.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(centaur.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(soulgorger, shelf)
                .doesNotContain(centaur);
    }
}
