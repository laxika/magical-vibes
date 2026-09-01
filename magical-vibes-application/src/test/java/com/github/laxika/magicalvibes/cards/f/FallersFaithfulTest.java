package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallersFaithful.class, GrizzlyBears.class})
class FallersFaithfulTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an undamaged creature and its controller draws two cards")
    void destroysUndamagedCreatureAndItsControllerDrawsTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castFallersFaithful(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB destroys a creature that was dealt damage without drawing")
    void destroysDamagedCreatureWithoutDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castFallersFaithful(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target another creature you control")
    void canTargetAnotherCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castFallersFaithful(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB does nothing when there is no other creature to target")
    void doesNothingWithoutAnotherCreature() {
        harness.setHand(player1, List.of(new FallersFaithful()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Faller's Faithful");
        assertThat(gd.stack).isEmpty();
    }

    private void castFallersFaithful(Permanent target) {
        harness.setHand(player1, List.of(new FallersFaithful()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
