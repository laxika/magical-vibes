package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MakindiAeronaut;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({ImmobilizerEldrazi.class, GrizzlyBears.class, WallOfWood.class, MakindiAeronaut.class})
class ImmobilizerEldraziTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with toughness greater than power can't block")
    void creaturesWithGreaterToughnessCannotBlock() {
        addReadyImmobilizer();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness greater than power");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 1)));
        assertThat(bears.isBlocking()).isTrue();
        assertThat(wall.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The restriction uses power and toughness when blockers are declared")
    void restrictionUsesCurrentPowerAndToughness() {
        addReadyImmobilizer();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        blocker.setToughnessModifier(1);
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness greater than power");
    }

    @Test
    @DisplayName("The ability requires colorless mana")
    void abilityRequiresColorlessMana() {
        addReadyImmobilizer();
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    private Permanent addReadyImmobilizer() {
        return addCreatureReady(player1, new ImmobilizerEldrazi());
    }

    @Test
    void creatureEnteringAfterResolutionCannotBlock() {
        addReadyImmobilizer();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player2, new MakindiAeronaut());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness greater than power");
    }

    @Test
    void creatureCanBlockAfterItsPowerReachesItsToughness() {
        addReadyImmobilizer();
        Permanent blocker = addCreatureReady(player2, new MakindiAeronaut());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        blocker.setPowerModifier(2);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void restrictionAlsoPreventsControllersCreaturesFromBlocking() {
        addReadyImmobilizer();
        addCreatureReady(player1, new MakindiAeronaut());
        addCreatureReady(player2, new ImmobilizerEldrazi());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness greater than power");
    }

    @Test
    void restrictionExpiresAfterTheTurnEnds() {
        addReadyImmobilizer();
        Permanent blocker = addCreatureReady(player1, new MakindiAeronaut());
        addCreatureReady(player2, new ImmobilizerEldrazi());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
