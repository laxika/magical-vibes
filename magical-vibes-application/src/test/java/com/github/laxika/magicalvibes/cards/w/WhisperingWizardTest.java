package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhisperingWizard.class, Shock.class, GrizzlyBears.class})
class WhisperingWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a flying Spirit token")
    void noncreatureSpellCreatesSpiritToken() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Spirit"), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        castShock();
        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersAgainOnLaterTurn() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        castShock();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Spirit token")
    void creatureSpellDoesNotCreateSpiritToken() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    private void castShock() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
