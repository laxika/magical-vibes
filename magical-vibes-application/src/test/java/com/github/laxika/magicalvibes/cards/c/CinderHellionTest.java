package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderHellion.class, ElspethKnightErrant.class, GrizzlyBears.class})
class CinderHellionTest extends BaseCardTest {

    @Test
    void entersBeforeChoosingEtbTarget() {
        harness.setHand(player1, List.of(new CinderHellion()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cinder Hellion");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
    }

    @Test
    void dealsTwoDamageToTargetOpponent() {
        harness.setLife(player2, 20);
        castAndChoose(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsTwoDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        castAndChoose(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void offersOnlyOpponentsAndPlaneswalkers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCinderHellion();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player2.getId())
                .doesNotContain(player1.getId(), creature.getId());
    }

    private void castAndChoose(java.util.UUID targetId) {
        castCinderHellion();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    private void castCinderHellion() {
        harness.setHand(player1, List.of(new CinderHellion()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
