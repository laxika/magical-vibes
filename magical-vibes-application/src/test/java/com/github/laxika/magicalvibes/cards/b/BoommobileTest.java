package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Boommobile")
class BoommobileTest extends BaseCardTest {

    @Test
    @DisplayName("ETB adds four same-color ability-only mana, which pays for its exhaust ability")
    void etbManaPaysForExhaustAbility() {
        harness.setHand(player1, List.of(new Boommobile()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.RED)).isEqualTo(4);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.passBothPriorities();

        Permanent boommobile = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(boommobile.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        harness.addToBattlefield(player1, new Boommobile());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
