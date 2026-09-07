package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeftstalkerArdent.class, GrizzlyBears.class, IchorWellspring.class, Ornithopter.class})
class WeftstalkerArdentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage when another creature you control enters")
    void damagesOpponentWhenAnotherCreatureEnters() {
        addArdent();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        resolveSpellAndTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Deals 1 damage when an artifact you control enters")
    void damagesOpponentWhenArtifactEnters() {
        addArdent();
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castArtifact(player1, 0);
        resolveSpellAndTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Deals damage only once when an artifact creature enters")
    void damagesOpponentOnceWhenArtifactCreatureEnters() {
        addArdent();
        harness.setHand(player1, List.of(new Ornithopter()));
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        resolveSpellAndTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's permanent")
    void doesNotTriggerForOpponentsPermanent() {
        addArdent();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Does not trigger when it enters")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new WeftstalkerArdent()));
        harness.addMana(player1, ManaColor.RED, 3);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    private void addArdent() {
        harness.addToBattlefield(player1, new WeftstalkerArdent());
    }

    private void resolveSpellAndTriggers() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
