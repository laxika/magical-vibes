package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DaringSaboteur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellowingMauler.class, DaringSaboteur.class})
class BellowingMaulerTest extends BaseCardTest {

    private static final String SACRIFICE = "Sacrifice a nontoken creature";
    private static final String LOSE_LIFE = "Lose 4 life";

    @Test
    @DisplayName("Each player may sacrifice a nontoken creature instead of losing life")
    void eachPlayerMaySacrificeNontokenCreature() {
        harness.addToBattlefield(player1, new BellowingMauler());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new DaringSaboteur());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new DaringSaboteur());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player1);

        harness.handleListChoice(player1, SACRIFICE);
        harness.handlePermanentChosen(player1, player1Creature.getId());
        harness.handleListChoice(player2, SACRIFICE);

        harness.assertLife(player1, player1Life);
        harness.assertLife(player2, player2Life);
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

        harness.assertLife(player1, player1Life - 4);
        harness.assertLife(player2, player2Life - 4);
    }

    @Test
    @DisplayName("Creature tokens cannot be sacrificed for this ability")
    void creatureTokensDoNotSatisfyTheSacrifice() {
        harness.addToBattlefield(player1, new BellowingMauler());
        Permanent token = harness.addToBattlefieldAndReturn(player2, creatureToken());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player1);
        harness.handleListChoice(player1, LOSE_LIFE);

        harness.assertLife(player2, player2Life - 4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    @Test
    @DisplayName("Bellowing Mauler can sacrifice itself without stopping the opponent's life loss")
    void maySacrificeItself() {
        harness.addToBattlefield(player1, new BellowingMauler());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player1);
        harness.handleListChoice(player1, SACRIFICE);

        harness.assertNotOnBattlefield(player1, "Bellowing Mauler");
        harness.assertInGraveyard(player1, "Bellowing Mauler");
        harness.assertLife(player1, player1Life);
        harness.assertLife(player2, player2Life - 4);
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new BellowingMauler());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, player1Life);
        harness.assertLife(player2, player2Life);
    }

    @Test
    @DisplayName("Sacrifices wait until every player has chosen")
    void sacrificesHappenAfterAllChoices() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new BellowingMauler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DaringSaboteur());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player1);
        harness.handleListChoice(player1, SACRIFICE);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mauler);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);

        harness.handleListChoice(player2, SACRIFICE);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mauler);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        harness.assertLife(player1, player1Life);
        harness.assertLife(player2, player2Life);
    }

    @Test
    @DisplayName("An opponent may keep their nontoken creature and lose life")
    void opponentMayDeclineSacrifice() {
        harness.addToBattlefield(player1, new BellowingMauler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DaringSaboteur());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        resolveEndStep(player1);
        harness.handleListChoice(player1, LOSE_LIFE);
        harness.handleListChoice(player2, LOSE_LIFE);

        harness.assertLife(player1, player1Life - 4);
        harness.assertLife(player2, player2Life - 4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
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
