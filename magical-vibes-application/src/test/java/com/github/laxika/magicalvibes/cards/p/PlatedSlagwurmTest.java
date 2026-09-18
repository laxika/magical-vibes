package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Battlegrowth;
import com.github.laxika.magicalvibes.cards.s.SpikeshotGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlatedSlagwurm.class, Battlegrowth.class, SpikeshotGoblin.class})
class PlatedSlagwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target Plated Slagwurm with spells")
    void opponentCannotTargetWithSpells() {
        Permanent slagwurm = addCreatureReady(player1, new PlatedSlagwurm());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Battlegrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, slagwurm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target Plated Slagwurm with spells")
    void controllerCanTargetWithSpells() {
        Permanent slagwurm = addCreatureReady(player1, new PlatedSlagwurm());

        harness.setHand(player1, List.of(new Battlegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, slagwurm.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent cannot target Plated Slagwurm with abilities")
    void opponentCannotTargetWithAbilities() {
        Permanent slagwurm = addCreatureReady(player1, new PlatedSlagwurm());
        addCreatureReady(player2, new SpikeshotGoblin());

        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, slagwurm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
