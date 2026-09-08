package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZaffaiAndTheTempestsTest extends BaseCardTest {

    @Test
    @DisplayName("The controller can cast an instant from hand without paying its mana cost")
    void castsInstantFromHandForFree() {
        harness.addToBattlefield(player1, new ZaffaiAndTheTempests());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller can cast a sorcery from hand without paying its mana cost")
    void castsSorceryFromHandForFree() {
        harness.addToBattlefield(player1, new ZaffaiAndTheTempests());
        harness.setHand(player1, List.of(new Divination()));

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The free cast is limited to one instant or sorcery each turn")
    void freeCastIsLimitedToOnceEachTurn() {
        harness.addToBattlefield(player1, new ZaffaiAndTheTempests());
        harness.setHand(player1, List.of(new Opt(), new Opt()));

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The permission does not apply to creature spells")
    void creatureSpellIsNotFree() {
        harness.addToBattlefield(player1, new ZaffaiAndTheTempests());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The permission does not apply during an opponent's turn")
    void permissionDoesNotApplyDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ZaffaiAndTheTempests());
        harness.setHand(player1, List.of(new Opt()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
