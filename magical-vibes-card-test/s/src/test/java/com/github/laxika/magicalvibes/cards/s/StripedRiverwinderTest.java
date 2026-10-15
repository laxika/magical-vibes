package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StripedRiverwinder.class, GrizzlyBears.class, Unsummon.class})
class StripedRiverwinderTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {U} discards Striped Riverwinder and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Striped Riverwinder");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling discards as a cost but draws only on resolution")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Striped Riverwinder");
        harness.assertNotInHand(player1, "Striped Riverwinder");
        harness.assertNotInHand(player1, "Unsummon");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Unsummon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the blue cycling cost")
    void cyclingRequiresBlueMana() {
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Striped Riverwinder");
        harness.assertNotInGraveyard(player1, "Striped Riverwinder");
        harness.assertNotInHand(player1, "Unsummon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Striped Riverwinder")
    void opponentCannotTargetRiverwinder() {
        var riverwinder = harness.addToBattlefieldAndReturn(player1, new StripedRiverwinder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, riverwinder.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Striped Riverwinder");
        harness.assertInHand(player2, "Unsummon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof allows Striped Riverwinder's controller to target it")
    void controllerCanTargetRiverwinder() {
        var riverwinder = harness.addToBattlefieldAndReturn(player1, new StripedRiverwinder());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, riverwinder.getId());

        harness.assertNotOnBattlefield(player1, "Striped Riverwinder");
        harness.assertInHand(player1, "Striped Riverwinder");
        harness.assertInGraveyard(player1, "Unsummon");
    }
}
