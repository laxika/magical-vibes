package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoratamiMindsweeperTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost, then target player mills two cards")
    void returnsLandThenMills() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player2.getId());

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chooses which land to return when several are available")
    void choosesLandWhenSeveralAvailable() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent plains = findPermanent(player1, "Plains");

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player2.getId());

        assertThat(gd.stack).isEmpty();

        harness.handlePermanentChosen(player1, plains.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Plains");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
