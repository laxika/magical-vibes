package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkullStorm.class, GrizzlyBears.class})
class SkullStormTest extends BaseCardTest {

    @Test
    void eachOpponentSacrificesAChosenCreatureAndControllerIsUnaffected() {
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        harness.handlePermanentChosen(player2, firstOpponentCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(controllerCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(secondOpponentCreature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void opponentWithoutCreatureLosesHalfLifeRoundedUp() {
        harness.setLife(player2, 7);

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(4);
    }

    @Test
    void copiesItselfForEachCommanderCastAndReevaluatesHalfLife() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.commanderTaxByCardId.put(commander.getId(), 4);

        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(2);
    }

    private void cast() {
        harness.setHand(player1, java.util.List.of(new SkullStorm()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castSorcery(player1, 0);
        while (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()) {
            if (gd.interaction.isAwaitingInput()) {
                break;
            }
            harness.passBothPriorities();
        }
    }
}
