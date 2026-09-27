package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellowingMauler.class, GrizzlyBears.class})
class BellowingMaulerTest extends BaseCardTest {

    private static final String SACRIFICE = "Sacrifice a nontoken creature";
    private static final String LOSE_LIFE = "Lose 4 life";

    @Test
    @DisplayName("Each player may sacrifice a nontoken creature instead of losing life")
    void eachPlayerMaySacrificeNontokenCreature() {
        harness.addToBattlefield(player1, new BellowingMauler());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player1);

        harness.handleListChoice(player1, SACRIFICE);
        harness.handlePermanentChosen(player1, player1Creature.getId());
        harness.handleListChoice(player2, SACRIFICE);

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2Creature);
    }

    @Test
    @DisplayName("A player who chooses life loses four life")
    void playerMayChooseLifeLoss() {
        harness.addToBattlefield(player1, new BellowingMauler());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player1);
        harness.handleListChoice(player1, LOSE_LIFE);

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life - 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 4);
    }

    @Test
    @DisplayName("Creature tokens cannot be sacrificed for this ability")
    void creatureTokensDoNotSatisfyTheSacrifice() {
        harness.addToBattlefield(player1, new BellowingMauler());
        Permanent token = harness.addToBattlefieldAndReturn(player2, creatureToken());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player1);
        harness.handleListChoice(player1, LOSE_LIFE);

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    private Card creatureToken() {
        Card token = new Card();
        token.setName("Creature Token");
        token.setType(CardType.CREATURE);
        token.setToken(true);
        token.setPower(1);
        token.setToughness(1);
        return token;
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
