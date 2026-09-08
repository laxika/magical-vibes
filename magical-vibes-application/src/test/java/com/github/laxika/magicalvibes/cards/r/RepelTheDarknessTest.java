package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepelTheDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two target creatures and draws a card")
    void tapsUpToTwoCreaturesAndDraws() {
        Permanent first = addReadyCreature(player2, new GrizzlyBears());
        Permanent second = addReadyCreature(player2, new AirElemental());
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Repel the Darkness");
    }

    @Test
    @DisplayName("Can resolve with no creature targets and still draws a card")
    void canResolveWithoutTargets() {
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
