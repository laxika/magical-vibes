package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfTheDireHour.class, BeaconOfUnrest.class, GrizzlyBears.class})
class AngelOfTheDireHourTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, exiles all attacking creatures and leaves nonattacking creatures")
    void castFromHandExilesAllAttackingCreatures() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        assertThat(attacker.isAttacking()).isTrue();

        harness.castFromHand(player1, new AngelOfTheDireHour(), "{5}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonattacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(attacker.getCard());
        harness.assertOnBattlefield(player1, "Angel of the Dire Hour");
    }

    @Test
    @DisplayName("Entering from the graveyard does not exile attacking creatures")
    void enteringFromGraveyardDoesNotExileAttackingCreatures() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        AngelOfTheDireHour angel = new AngelOfTheDireHour();
        harness.setGraveyard(player1, List.of(angel));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, 0, angel.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        harness.assertOnBattlefield(player1, "Angel of the Dire Hour");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(attacker.getCard());
    }
    @Test
    @DisplayName("Exiles every attacker when multiple creatures attack")
    void exilesMultipleAttackers() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0, 1)));

        harness.castFromHand(player1, new AngelOfTheDireHour(), "{5}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonattacker)
                .doesNotContain(first, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(first.getCard(), second.getCard());
        harness.assertOnBattlefield(player1, "Angel of the Dire Hour");
    }

    @Test
    @DisplayName("Also exiles its controller's attacking creatures")
    void exilesControllersOwnAttackers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        harness.castFromHand(player1, new AngelOfTheDireHour(), "{5}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonattacker).doesNotContain(attacker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(attacker.getCard());
        harness.assertOnBattlefield(player1, "Angel of the Dire Hour");
    }

    @Test
    @DisplayName("Entering with no attackers leaves all creatures on the battlefield")
    void noAttackersLeavesCreaturesUntouched() {
        Permanent friendly = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new AngelOfTheDireHour(), "{5}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(friendly);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Angel of the Dire Hour");
    }
}
