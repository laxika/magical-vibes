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

        harness.setHand(player1, List.of(new AngelOfTheDireHour()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        harness.assertOnBattlefield(player1, "Angel of the Dire Hour");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(attacker.getCard());
    }
}
