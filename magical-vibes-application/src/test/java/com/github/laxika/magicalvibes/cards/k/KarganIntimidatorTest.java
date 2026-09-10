package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarganIntimidator.class, GrizzlyBears.class})
class KarganIntimidatorTest extends BaseCardTest {

    @Test
    @DisplayName("The pump ability gives Kargan Intimidator +1/+1 until end of turn")
    void pumpsSelf() {
        Permanent kargan = addReadyKargan(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(kargan.getPowerModifier()).isEqualTo(1);
        assertThat(kargan.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature made a Coward can't block a Warrior")
    void cowardCannotBlockWarrior() {
        Permanent kargan = addReadyKargan(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        kargan.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cowards can't block Warriors");
    }

    @Test
    @DisplayName("The Warrior ability grants trample until end of turn")
    void warriorGainsTrample() {
        Permanent kargan = addReadyKargan(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, kargan.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kargan, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Each mode can be used once, but different modes can be used in the same turn")
    void eachModeCanBeUsedOncePerTurn() {
        Permanent kargan = addReadyKargan(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(kargan.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.COWARD);
    }

    @Test
    @DisplayName("The trample ability only targets Warrior creatures")
    void trampleAbilityRequiresWarrior() {
        addReadyKargan(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKargan(Player player) {
        return addCreatureReady(player, new KarganIntimidator());
    }
}
