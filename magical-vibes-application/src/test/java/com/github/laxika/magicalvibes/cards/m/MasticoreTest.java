package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasticoreTest extends BaseCardTest {

    @Test
    void upkeepCanBePaidByDiscardingAnyCard() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void upkeepSacrificesMasticoreWhenDiscardIsDeclined() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Masticore");
    }

    @Test
    void upkeepSacrificesMasticoreWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Masticore");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void damageAbilityDealsOneDamageToTargetCreature() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void damageAbilityRejectsPlayerTarget() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }

    @Test
    void regenerationAbilityGrantsARegenerationShield() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }
}
