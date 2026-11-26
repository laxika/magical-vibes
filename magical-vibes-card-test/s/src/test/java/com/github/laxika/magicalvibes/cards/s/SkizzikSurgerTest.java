package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkizzikSurger.class, DryadArbor.class, Imperiosaur.class})
class SkizzikSurgerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts no echo trigger on the stack")
    void enteringDoesNotCreateAnEnterTheBattlefieldTrigger() {
        harness.setHand(player1, List.of(new SkizzikSurger()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skizzik Surger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's lands can't pay the echo cost")
    void opponentsLandsCannotPayEcho() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new DryadArbor());
        harness.addToBattlefield(player2, new DryadArbor());
        harness.addToBattlefield(player2, new DryadArbor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(landCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Echo waits for its controller's upkeep")
    void opponentsUpkeepDoesNotConsumeEcho() {
        castSkizzikSurger();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Skizzik Surger");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skizzik Surger");
    }

    @Test
    @DisplayName("With fewer than two lands it is sacrificed unasked")
    void insufficientLandsSacrificeSkizzikSurger() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new DryadArbor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skizzik Surger");
        harness.assertInGraveyard(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying echo sacrifices two lands and keeps it")
    void acceptingEchoSacrificesTwoLandsAndKeepsSkizzikSurger() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new DryadArbor());
        harness.addToBattlefield(player1, new DryadArbor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        answerForcedPick(player1);

        harness.assertOnBattlefield(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isZero();
    }

    @Test
    @DisplayName("The payer chooses exactly two lands to sacrifice")
    void choosingEchoLandsSacrificesExactlyTwo() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new DryadArbor());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new DryadArbor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId(), secondLand.getId()));

        harness.assertOnBattlefield(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining echo sacrifices it")
    void decliningEchoSacrificesSkizzikSurger() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new DryadArbor());
        harness.addToBattlefield(player1, new DryadArbor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Skizzik Surger");
        harness.assertInGraveyard(player1, "Skizzik Surger");
        assertThat(landCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Echo is paid only once")
    void echoIsOneShot() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new DryadArbor());
        harness.addToBattlefield(player1, new DryadArbor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        answerForcedPick(player1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Skizzik Surger");
    }

    @Test
    @DisplayName("Only lands can pay the echo cost")
    void echoOnlyAllowsLandsToBeSacrificed() {
        castSkizzikSurger();
        harness.addToBattlefield(player1, new DryadArbor());
        harness.addToBattlefield(player1, new DryadArbor());
        harness.addToBattlefield(player1, new Imperiosaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        answerForcedPick(player1);

        harness.assertOnBattlefield(player1, "Skizzik Surger");
        harness.assertOnBattlefield(player1, "Imperiosaur");
        assertThat(landCount(player1)).isZero();
    }

    @Test
    @DisplayName("Haste lets it attack the turn it enters")
    void hasteAllowsSkizzikSurgerToAttackTheTurnItEnters() {
        castSkizzikSurger();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    private void castSkizzikSurger() {
        harness.setHand(player1, List.of(new SkizzikSurger()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }
}
