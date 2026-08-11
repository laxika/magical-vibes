package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObsidianAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from black")
    void hasProtectionFromBlack() {
        Permanent acolyte = addCreatureReady(player1, new ObsidianAcolyte());

        harness.setHand(player2, java.util.List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, acolyte.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{W}: target creature gains protection from black until end of turn")
    void grantsProtectionFromBlackToTargetCreature() {
        addCreatureReady(player1, new ObsidianAcolyte());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Granted protection wears off at end of turn")
    void grantedProtectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ObsidianAcolyte());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLACK);
    }

    @Test
    @DisplayName("Protection from black stops black removal after the ability resolves")
    void grantedProtectionStopsBlackRemoval() {
        addCreatureReady(player1, new ObsidianAcolyte());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, java.util.List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
