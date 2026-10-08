package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyosLogbook.class, Forest.class})
class TamiyosLogbookTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card with its activation cost reduced by other artifacts")
    void drawsCardWithReducedCost() {
        Permanent logbook = harness.addToBattlefieldAndReturn(player1, new TamiyosLogbook());
        harness.addToBattlefield(player1, new TamiyosLogbook());
        harness.addToBattlefield(player1, new TamiyosLogbook());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(logbook.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not count Tamiyo's Logbook when reducing its own activation cost")
    void doesNotCountItself() {
        harness.addToBattlefield(player1, new TamiyosLogbook());
        harness.addToBattlefield(player1, new TamiyosLogbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysFullCostWithNoOtherArtifacts() {
        Permanent logbook = harness.addToBattlefieldAndReturn(player1, new TamiyosLogbook());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(logbook.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void excessArtifactsReduceCostToOneBlueMana() {
        harness.addToBattlefield(player1, new TamiyosLogbook());
        for (int i = 0; i < 6; i++) {
            Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TamiyosLogbook());
            artifact.tap();
        }
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void costReductionCannotRemoveBlueManaRequirement() {
        harness.addToBattlefield(player1, new TamiyosLogbook());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new TamiyosLogbook());
        }
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsArtifactsDoNotReduceCost() {
        harness.addToBattlefield(player1, new TamiyosLogbook());
        harness.addToBattlefield(player2, new TamiyosLogbook());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonartifactPermanentsDoNotReduceCost() {
        harness.addToBattlefield(player1, new TamiyosLogbook());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedLogbookCannotActivate() {
        Permanent logbook = harness.addToBattlefieldAndReturn(player1, new TamiyosLogbook());
        logbook.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
