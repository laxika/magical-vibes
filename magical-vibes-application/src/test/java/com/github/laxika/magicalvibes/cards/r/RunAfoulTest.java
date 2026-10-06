package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.cards.d.DrowsingTyrannodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunAfoul.class, ConcordiaPegasus.class, DrowsingTyrannodon.class})
class RunAfoulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an opponent's flying creature")
    void sacrificesFlyingCreature() {
        harness.addToBattlefield(player2, new ConcordiaPegasus());
        harness.addToBattlefield(player2, new DrowsingTyrannodon());
        castAndResolveRunAfoul();

        harness.assertNotOnBattlefield(player2, "Concordia Pegasus");
        harness.assertOnBattlefield(player2, "Drowsing Tyrannodon");
        harness.assertInGraveyard(player2, "Concordia Pegasus");
    }

    @Test
    @DisplayName("Opponent chooses among multiple flying creatures")
    void opponentChoosesFlyingCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ConcordiaPegasus());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ConcordiaPegasus());
        castAndResolveRunAfoul();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(second.getId())
                .doesNotContain(first.getId());
    }

    @Test
    @DisplayName("Does nothing when the opponent controls no flying creature")
    void noFlyingCreature() {
        harness.addToBattlefield(player2, new DrowsingTyrannodon());
        castAndResolveRunAfoul();

        harness.assertOnBattlefield(player2, "Drowsing Tyrannodon");
        harness.assertInGraveyard(player1, "Run Afoul");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new RunAfoul()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Resolves against an opponent with an empty battlefield")
    void emptyOpponentBattlefield() {
        castAndResolveRunAfoul();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Run Afoul");
    }

    @Test
    @DisplayName("Does not sacrifice the caster's flying creature")
    void onlyOpponentSacrifices() {
        harness.addToBattlefield(player1, new ConcordiaPegasus());
        harness.addToBattlefield(player2, new ConcordiaPegasus());

        castAndResolveRunAfoul();

        harness.assertOnBattlefield(player1, "Concordia Pegasus");
        harness.assertNotInGraveyard(player1, "Concordia Pegasus");
        harness.assertNotOnBattlefield(player2, "Concordia Pegasus");
        harness.assertInGraveyard(player2, "Concordia Pegasus");
    }

    private void castAndResolveRunAfoul() {
        harness.setHand(player1, List.of(new RunAfoul()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
