package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UltimoCivilizationsEnd.class, GrizzlyBears.class})
class UltimoCivilizationsEndTest extends BaseCardTest {

    @Test
    @DisplayName("When Ultimo enters, each opponent sacrifices a creature")
    void eachOpponentSacrificesCreatureOnEnter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UltimoCivilizationsEnd()));
        addUltimoMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ultimo's ETB does not sacrifice its controller's creatures")
    void controllerCreaturesAreUnaffected() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UltimoCivilizationsEnd()));
        addUltimoMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("Ultimo's hand ability discards Ultimo and makes each opponent sacrifice a creature")
    void handAbilityDiscardsUltimoAndSacrificesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UltimoCivilizationsEnd()));
        addUltimoMana();

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Ultimo, Civilization's End");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each opponent chooses which creature to sacrifice when multiple are available")
    void opponentChoosesCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UltimoCivilizationsEnd()));
        addUltimoMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Hand ability works with exactly two generic and one black mana")
    void handAbilityUsesItsOwnManaCost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UltimoCivilizationsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Ultimo, Civilization's End");
        harness.assertInGraveyard(player1, "Ultimo, Civilization's End");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Hand activation without black mana does not discard Ultimo")
    void handAbilityRequiresBlackMana() {
        harness.setHand(player1, List.of(new UltimoCivilizationsEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ultimo, Civilization's End");
        harness.assertNotInGraveyard(player1, "Ultimo, Civilization's End");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves normally when the opponent has no creatures")
    void entersWithNoOpponentCreatures() {
        harness.setHand(player1, List.of(new UltimoCivilizationsEnd()));
        addUltimoMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ultimo, Civilization's End");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Hand ability may be activated on the opponent's turn with no creatures")
    void handAbilityOnOpponentTurnWithNoCreatures() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new UltimoCivilizationsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ultimo, Civilization's End");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void cannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new UltimoCivilizationsEnd());
        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Menace permits two blockers")
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new UltimoCivilizationsEnd());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Ultimo, Civilization's End");
    }

    private void addUltimoMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
