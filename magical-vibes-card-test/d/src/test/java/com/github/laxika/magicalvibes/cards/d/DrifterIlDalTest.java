package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrifterIlDal.class, AshcoatBear.class})
class DrifterIlDalTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {U} sacrifices Drifter il-Dal")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drifter il-Dal");
        harness.assertInGraveyard(player1, "Drifter il-Dal");
    }

    @Test
    @DisplayName("Paying {U} keeps Drifter il-Dal on the battlefield")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Drifter il-Dal");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without blue mana sacrifices Drifter il-Dal")
    void acceptWithoutManaSacrifices() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Drifter il-Dal");
    }

    @Test
    @DisplayName("Drifter il-Dal does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drifter il-Dal");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Shadow prevents a non-shadow creature from blocking Drifter il-Dal")
    void cannotBeBlockedByNonShadowCreature() {
        addCreatureReady(player1, new DrifterIlDal());
        addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shadow prevents Drifter il-Dal from blocking a non-shadow creature")
    void cannotBlockNonShadowCreature() {
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new DrifterIlDal());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-blue mana cannot pay the upkeep cost")
    void nonBlueManaCannotPayUpkeep() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Drifter il-Dal");
        harness.assertInGraveyard(player1, "Drifter il-Dal");
    }

    @Test
    @DisplayName("The upkeep payment can be declined even when blue mana is available")
    void mayDeclineWithManaAvailable() {
        harness.addToBattlefield(player1, new DrifterIlDal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drifter il-Dal");
        harness.assertInGraveyard(player1, "Drifter il-Dal");
    }

    @Test
    @DisplayName("A creature with shadow can block Drifter il-Dal")
    void canBeBlockedByShadowCreature() {
        addCreatureReady(player1, new DrifterIlDal());
        addCreatureReady(player2, new DrifterIlDal());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Drifter il-Dal");
        harness.assertInGraveyard(player2, "Drifter il-Dal");
    }
}
