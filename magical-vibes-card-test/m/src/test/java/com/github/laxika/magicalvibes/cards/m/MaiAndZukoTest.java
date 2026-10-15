package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
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

    @Test
    void firebendingAddsThreeRedManaThroughCombatOnly() {
        addCreatureReady(player1, new MaiAndZuko());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void canCastAllyDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new MaiAndZuko());
        harness.forceActivePlayer(player2);
        prepareForInstantSpeedCast();
        harness.setHand(player1, List.of(new MaiAndZuko()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotGrantFlashToOpponentsArtifacts() {
        harness.addToBattlefield(player1, new MaiAndZuko());
        prepareForInstantSpeedCast();
        harness.setHand(player2, List.of(new LeoninScimitar()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void flashPermissionEndsWhenSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new MaiAndZuko());
        prepareForInstantSpeedCast();
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
