package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.d.DaruSpiritualist;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonTyrant.class, AvenFarseer.class, DaruSpiritualist.class})
class DragonTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Dragon Tyrant")
    void decliningUpkeepPaymentSacrificesIt() {
        harness.addToBattlefield(player1, new DragonTyrant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Dragon Tyrant");
        harness.assertInGraveyard(player1, "Dragon Tyrant");
    }

    @Test
    @DisplayName("Accepting without four red mana still sacrifices Dragon Tyrant")
    void acceptingUpkeepPaymentWithoutEnoughManaSacrificesIt() {
        harness.addToBattlefield(player1, new DragonTyrant());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Dragon Tyrant");
        harness.assertInGraveyard(player1, "Dragon Tyrant");
    }

    @Test
    @DisplayName("Paying {R}{R}{R}{R} keeps Dragon Tyrant on the battlefield")
    void payingUpkeepPaymentKeepsItOnBattlefield() {
        harness.addToBattlefield(player1, new DragonTyrant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 4);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Dragon Tyrant");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Dragon Tyrant's upkeep ability does not trigger during an opponent's upkeep")
    void upkeepAbilityDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent tyrant = addCreatureReady(player1, new DragonTyrant());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tyrant);
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Dragon Tyrant")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new DragonTyrant());
        addCreatureReady(player2, new DaruSpiritualist());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                        gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("The pump ability spends red mana and wears off at cleanup")
    void pumpAbilityWearsOffAtCleanup() {
        Permanent tyrant = addCreatureReady(player1, new DragonTyrant());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tyrant)).isEqualTo(7);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tyrant)).isEqualTo(6);
    }

    @Test
    @DisplayName("Double strike and trample deal damage in both combat damage steps")
    void doubleStrikeAndTrampleDealDamageInBothSteps() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DragonTyrant());
        Permanent blocker = addCreatureReady(player2, new AvenFarseer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 5));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
