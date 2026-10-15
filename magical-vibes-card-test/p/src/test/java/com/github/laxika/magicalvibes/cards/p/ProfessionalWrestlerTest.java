package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfessionalWrestler.class})
class ProfessionalWrestlerTest extends BaseCardTest {

    @Test
    @DisplayName("When Professional Wrestler enters, it creates a Treasure token")
    void entersWithTreasureToken() {
        harness.setHand(player1, List.of(new ProfessionalWrestler()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Professional Wrestler can be blocked by one creature")
    void canBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new ProfessionalWrestler());
        attacker.setAttacking(true);

        addCreatureReady(player2, new ProfessionalWrestler());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Professional Wrestler cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new ProfessionalWrestler());
        attacker.setAttacking(true);

        addCreatureReady(player2, new ProfessionalWrestler());
        addCreatureReady(player2, new ProfessionalWrestler());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    void noncastEntryCreatesUntappedTreasureOnlyForItsController() {
        harness.enterBattlefieldAndReturn(player2, new ProfessionalWrestler());

        assertThat(countPermanents(player2, "Treasure")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player2, "Treasure").isTapped()).isFalse();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void enterTriggerStillCreatesTreasureAfterSourceDies() {
        Permanent wrestler = harness.enterBattlefieldAndReturn(player1, new ProfessionalWrestler());
        wrestler.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Professional Wrestler");

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForAnyColor(ManaColor color) {
        harness.enterBattlefieldAndReturn(player1, new ProfessionalWrestler());
        resolveAllTriggers();

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoWrestlersCanEachBeBlockedByOneCreature() {
        addCreatureReady(player1, new ProfessionalWrestler()).setAttacking(true);
        addCreatureReady(player1, new ProfessionalWrestler()).setAttacking(true);
        addCreatureReady(player2, new ProfessionalWrestler());
        addCreatureReady(player2, new ProfessionalWrestler());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
    }
}
