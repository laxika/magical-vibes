package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArekFalseGoldwarden.class, GrizzlyBears.class})
class ArekFalseGoldwardenTest extends BaseCardTest {

    @Test
    void anotherCreatureYouControlIntensifiesArek() {
        Permanent arek = addCreatureReady(player1, new ArekFalseGoldwarden());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(arek.getCard().getId())).isEqualTo(1);
    }

    @Test
    void enteringArekDoesNotIntensifyItself() {
        ArekFalseGoldwarden arek = new ArekFalseGoldwarden();
        harness.castFromHand(player1, arek, "{W}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arek, False Goldwarden");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardIntensity(arek.getId())).isZero();
    }

    @Test
    void opponentsCreatureDoesNotIntensifyArek() {
        Permanent arek = addCreatureReady(player2, new ArekFalseGoldwarden());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardIntensity(arek.getCard().getId())).isZero();
    }

    @Test
    void triggerIntensifiesOwnedCopiesInOtherZonesButNotOpponentsCopies() {
        Permanent arek = addCreatureReady(player1, new ArekFalseGoldwarden());
        ArekFalseGoldwarden inHand = new ArekFalseGoldwarden();
        ArekFalseGoldwarden inLibrary = new ArekFalseGoldwarden();
        ArekFalseGoldwarden inGraveyard = new ArekFalseGoldwarden();
        ArekFalseGoldwarden opponentsCopy = new ArekFalseGoldwarden();
        harness.setLibrary(player1, List.of(inLibrary));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.setHand(player2, List.of(opponentsCopy));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.setHand(player1, List.of(inHand));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(arek.getCard().getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(inHand.getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(inLibrary.getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(inGraveyard.getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(opponentsCopy.getId())).isZero();
    }

    @Test
    void zeroIntensityStillPaysSacrificeCostWithoutChangingLife() {
        addCreatureReady(player1, new ArekFalseGoldwarden());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Arek, False Goldwarden");
        harness.assertInGraveyard(player1, "Arek, False Goldwarden");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificeAbilityDrainsOpponentByArekIntensity() {
        Permanent arek = addCreatureReady(player1, new ArekFalseGoldwarden());
        gd.intensifyCard(arek.getCard(), 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Arek, False Goldwarden");
    }

    @Test
    void sacrificeAbilityCannotTargetItsController() {
        addCreatureReady(player1, new ArekFalseGoldwarden());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
