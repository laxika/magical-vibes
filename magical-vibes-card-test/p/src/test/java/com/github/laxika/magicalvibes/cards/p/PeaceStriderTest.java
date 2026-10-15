package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeaceStrider.class, DivineOffering.class})
class PeaceStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Peace Strider puts it on the stack as an artifact spell")
    void castingPutsOnStack() {
        castPeaceStrider();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Peace Strider");
    }

    @Test
    @DisplayName("Resolving puts Peace Strider on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castPeaceStrider();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Peace Strider");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Peace Strider");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB trigger causes controller to gain 3 life")
    void etbGainsLife() {
        castPeaceStrider();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);

        castPeaceStrider();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castPeaceStrider();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB gains life for the other player when they control Peace Strider")
    void etbGainsLifeForOtherController() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new PeaceStrider()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Entering without being cast also triggers the life gain")
    void etbGainsLifeWithoutBeingCast() {
        harness.getBattlefieldEntryService().putPermanentOntoBattlefield(
                gd, player1.getId(), new Permanent(new PeaceStrider()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Peace Strider");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB still gains life after Peace Strider is destroyed in response")
    void etbGainsLifeAfterSourceLeavesBattlefield() {
        castPeaceStrider();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new DivineOffering()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Peace Strider").getId());

        harness.assertNotOnBattlefield(player1, "Peace Strider");
        harness.assertInGraveyard(player1, "Peace Strider");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 24);
        assertThat(gd.stack).isEmpty();
    }

    private void castPeaceStrider() {
        harness.setHand(player1, List.of(new PeaceStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
    }
}
