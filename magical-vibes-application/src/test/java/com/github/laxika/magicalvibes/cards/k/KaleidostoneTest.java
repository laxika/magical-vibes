package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Kaleidostone.class})
class KaleidostoneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new Kaleidostone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Kaleidostone()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact, ETB trigger onto stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        // One card cast, one drawn: net hand size returns to what it was before casting.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertInHand(player1, "Kaleidostone");
    }

    @Test
    @DisplayName("Paying {5}, tapping and sacrificing adds one mana of each color")
    void activatingAddsOneManaOfEachColor() {
        harness.addToBattlefield(player1, new Kaleidostone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the ability sacrifices Kaleidostone")
    void activatingSacrificesKaleidostone() {
        harness.addToBattlefield(player1, new Kaleidostone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Kaleidostone");
        harness.assertInGraveyard(player1, "Kaleidostone");
    }

    @Test
    @DisplayName("Four mana cannot pay the activation cost")
    void cannotActivateWithInsufficientMana() {
        var stone = harness.addToBattlefieldAndReturn(player1, new Kaleidostone());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Kaleidostone");
        assertThat(stone.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Kaleidostone cannot activate its mana ability")
    void cannotActivateWhileTapped() {
        var stone = harness.addToBattlefieldAndReturn(player1, new Kaleidostone());
        stone.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Kaleidostone");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing the newly entered artifact does not stop its pending draw trigger")
    void canActivateBeforeEnterTriggerResolves() {
        harness.setHand(player1, List.of(new Kaleidostone()));
        harness.setLibrary(player1, List.of(new Kaleidostone(), new Kaleidostone()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Kaleidostone");
        harness.assertNotOnBattlefield(player1, "Kaleidostone");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Kaleidostone");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
