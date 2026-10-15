package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MeteorGolem;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OneWithTheMachine.class, Ornithopter.class, ObeliskOfBant.class,
        GrizzlyBears.class, MeteorGolem.class, Naturalize.class})
class OneWithTheMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the greatest mana value among artifacts you control")
    void drawsForGreatestArtifactManaValue() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.setHand(player1, List.of(new OneWithTheMachine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Non-artifacts and opponent artifacts are ignored")
    void ignoresNonArtifactsAndOpponentArtifacts() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ObeliskOfBant());
        harness.setHand(player1, List.of(new OneWithTheMachine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws no cards with no artifacts, and goes to the graveyard")
    void drawsNothingWithoutArtifacts() {
        harness.setHand(player1, List.of(new OneWithTheMachine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "One with the Machine");
    }

    @Test
    @DisplayName("Tied artifact mana values are counted once rather than added together")
    void drawsGreatestValueRatherThanTotal() {
        harness.addToBattlefield(player1, new MeteorGolem());
        harness.addToBattlefield(player1, new MeteorGolem());
        harness.setHand(player1, List.of(new OneWithTheMachine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Artifacts destroyed in response do not contribute to the draw count")
    void checksArtifactsAtResolution() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new MeteorGolem());
        harness.setHand(player1, List.of(new OneWithTheMachine()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Meteor Golem");
        harness.assertInGraveyard(player1, "One with the Machine");
    }
}
