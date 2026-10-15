package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallersFaithful.class, IntrepidTenderfoot.class})
class FallersFaithfulTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an undamaged creature and its controller draws two cards")
    void destroysUndamagedCreatureAndItsControllerDrawsTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new IntrepidTenderfoot(), new IntrepidTenderfoot()));

        castFallersFaithful(target);

        harness.assertNotOnBattlefield(player2, "Intrepid Tenderfoot");
        harness.assertInGraveyard(player2, "Intrepid Tenderfoot");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB destroys a creature that was dealt damage without drawing")
    void destroysDamagedCreatureWithoutDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new IntrepidTenderfoot(), new IntrepidTenderfoot()));

        castFallersFaithful(target);

        harness.assertNotOnBattlefield(player2, "Intrepid Tenderfoot");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target another creature you control")
    void canTargetAnotherCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        harness.setLibrary(player1, List.of(new IntrepidTenderfoot(), new IntrepidTenderfoot()));

        castFallersFaithful(target);

        harness.assertNotOnBattlefield(player1, "Intrepid Tenderfoot");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB does nothing when there is no other creature to target")
    void doesNothingWithoutAnotherCreature() {
        harness.castFromHand(player1, new FallersFaithful(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Faller's Faithful");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can choose no target even when another creature is available")
    void canDeclineAvailableTarget() {
        harness.addToBattlefield(player2, new IntrepidTenderfoot());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new IntrepidTenderfoot(), new IntrepidTenderfoot()));

        harness.castFromHand(player1, new FallersFaithful(), "{2}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Intrepid Tenderfoot");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target itself, and a rejected choice still permits a legal target")
    void cannotTargetItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        harness.setLibrary(player2, List.of(new IntrepidTenderfoot(), new IntrepidTenderfoot()));
        harness.castFromHand(player1, new FallersFaithful(), "{2}{B}");
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Faller's Faithful");
        harness.assertInGraveyard(player2, "Intrepid Tenderfoot");
    }

    @Test
    @DisplayName("An undamaged indestructible target survives and its controller still draws two")
    void indestructibleTargetStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new IntrepidTenderfoot(), new IntrepidTenderfoot()));

        castFallersFaithful(target);

        harness.assertOnBattlefield(player2, "Intrepid Tenderfoot");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Damage dealt after targeting is checked when the trigger resolves")
    void checksDamageAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new IntrepidTenderfoot(), new IntrepidTenderfoot()));
        harness.castFromHand(player1, new FallersFaithful(), "{2}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Intrepid Tenderfoot");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    private void castFallersFaithful(Permanent target) {
        harness.castFromHand(player1, new FallersFaithful(), "{2}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
