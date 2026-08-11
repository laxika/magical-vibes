package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeToFeedTest extends BaseCardTest {

    @Test
    @DisplayName("The fight kills the opponent's creature and the delayed trigger gains 3 life")
    void fightKillsTargetAndGainsLife() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();

        harness.castSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The delayed trigger does not gain life when the fought creature survives")
    void noLifeGainWhenTargetSurvives() {
        Permanent ownCreature = addCreatureReady(player1, new LlanowarElves());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();

        harness.castSorcery(player1, 0, List.of(opponentCreature.getId(), ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The second target must be a creature you control")
    void secondTargetMustBeControlledCreature() {
        Permanent firstOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TimeToFeed()));
        addManaForTimeToFeed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstOpponentCreature.getId(), secondOpponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForTimeToFeed() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
