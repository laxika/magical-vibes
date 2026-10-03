package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrixInterlacer.class, Forest.class, Ornithopter.class})
class DrixInterlacerTest extends BaseCardTest {

    @Test
    void anotherArtifactYouControlAddsIntensity() {
        Permanent drix = harness.addToBattlefieldAndReturn(player1, new DrixInterlacer());
        harness.castFromHand(player1, new Ornithopter(), "{0}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(drix.getCard())).isEqualTo(2);
    }

    @Test
    void sacrificeDrawsHalfIntensityRoundedDown() {
        Permanent drix = harness.addToBattlefieldAndReturn(player1, new DrixInterlacer());
        gd.intensifyCard(drix.getCard(), 3);
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drix Interlacer");
        harness.assertInGraveyard(player1, "Drix Interlacer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1)
                .contains(drawn);
    }

    @Test
    void abilityIsSorcerySpeedOnly() {
        harness.addToBattlefieldAndReturn(player1, new DrixInterlacer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4})
    void sacrificeUsesPersistentIntensity(int intensity) {
        Permanent drix = harness.addToBattlefieldAndReturn(player1, new DrixInterlacer());
        gd.intensifyCard(drix.getCard(), intensity);
        harness.setHand(player1, List.of());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Drix Interlacer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(intensity / 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3 - intensity / 2);
        assertThat(gd.getCardIntensity(drix.getCard())).isEqualTo(intensity);
    }

    @Test
    void ownEntryOpponentArtifactAndOwnLandDoNotIntensify() {
        DrixInterlacer card = new DrixInterlacer();
        harness.castFromHand(player1, card, "{1}");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardIntensity(card)).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new DrixInterlacer(), "{1}");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardIntensity(card)).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardIntensity(card)).isZero();
    }

    @Test
    void tappedArtifactCannotActivate() {
        Permanent drix = harness.addToBattlefieldAndReturn(player1, new DrixInterlacer());
        drix.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Drix Interlacer");
    }

    @Test
    void abilityCannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new DrixInterlacer());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityCannotActivateWithArtifactSpellOnStack() {
        harness.addToBattlefield(player1, new DrixInterlacer());
        harness.castFromHand(player1, new DrixInterlacer(), "{1}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Drix Interlacer");
    }
}
