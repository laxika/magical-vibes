package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmobilizerEldrazi.class, GrizzlyBears.class, WallOfWood.class})
class ImmobilizerEldraziTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with toughness greater than power can't block")
    void creaturesWithGreaterToughnessCannotBlock() {
        addReadyImmobilizer();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

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
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        blocker.setToughnessModifier(1);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

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
}
