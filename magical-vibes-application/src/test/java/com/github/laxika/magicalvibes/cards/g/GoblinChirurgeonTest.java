package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Aeolipile;
import com.github.laxika.magicalvibes.cards.f.FarrelitePriest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinChirurgeon.class, FarrelitePriest.class, Aeolipile.class})
class GoblinChirurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin regenerates the target creature")
    void sacrificesGoblinAndRegeneratesTarget() {
        harness.addToBattlefield(player1, new GoblinChirurgeon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FarrelitePriest());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Goblin Chirurgeon");

        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Chirurgeon cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GoblinChirurgeon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Aeolipile());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Sacrifice cost only accepts Goblins")
    void sacrificeCostOnlyAcceptsGoblins() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent otherGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new FarrelitePriest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FarrelitePriest());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, otherGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(source, nonGoblin)
                .doesNotContain(otherGoblin);
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target the source while sacrificing another Goblin")
    void canTargetSourceWhileSacrificingAnotherGoblin() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent sacrificedGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handlePermanentChosen(player1, sacrificedGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(source)
                .doesNotContain(sacrificedGoblin);
        assertThat(source.getRegenerationShield()).isEqualTo(1);
    }
}
