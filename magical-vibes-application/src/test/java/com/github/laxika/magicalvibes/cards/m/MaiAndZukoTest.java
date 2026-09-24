package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaiAndZuko.class, HadaFreeblade.class, LeoninScimitar.class, GrizzlyBears.class})
class MaiAndZukoTest extends BaseCardTest {

    @Test
    void canCastAllySpellAtInstantSpeed() {
        harness.addToBattlefield(player1, new MaiAndZuko());
        prepareForInstantSpeedCast();

        harness.setHand(player1, List.of(new HadaFreeblade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canCastArtifactSpellAtInstantSpeed() {
        harness.addToBattlefield(player1, new MaiAndZuko());
        prepareForInstantSpeedCast();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotCastNonAllyNonArtifactCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new MaiAndZuko());
        prepareForInstantSpeedCast();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void prepareForInstantSpeedCast() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }
}
