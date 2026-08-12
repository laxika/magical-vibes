package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.GREEN;
import static org.assertj.core.api.Assertions.assertThat;

class GenesisChamberTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering gives its controller a Myr token")
    void nontokenCreatureEnteringGivesItsControllerMyrToken() {
        addChamber(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(myrTokens(player1)).hasSize(1);
        assertThat(myrTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("A nontoken creature entering under an opponent's control gives that player a Myr token")
    void opponentCreatureEnteringGivesOpponentMyrToken() {
        addChamber(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(myrTokens(player1)).isEmpty();
        assertThat(myrTokens(player2)).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Genesis Chamber does not trigger")
    void tappedChamberDoesNotTrigger() {
        addChamber(player1);
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(myrTokens(player1)).isEmpty();
    }

    private void addChamber(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new GenesisChamber()));
    }

    private List<Permanent> myrTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Myr"))
                .toList();
    }
}
