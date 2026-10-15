package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistmeadowCouncil.class, Forest.class, GoldmeadowStalwart.class})
class MistmeadowCouncilTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an ETB draw trigger")
    void entersWithDrawTrigger() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistmeadow Council");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Costs the full amount without a Kithkin")
    void costsFullAmountWithoutKithkin() {
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs one less while controlling a Kithkin")
    void costsOneLessWithKithkin() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's Kithkin does not reduce the cost")
    void opponentKithkinDoesNotReduceCost() {
        harness.addToBattlefield(player2, new MistmeadowCouncil());
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A Kithkin in hand does not reduce the cost")
    void kithkinInHandDoesNotReduceCost() {
        harness.setHand(player1, List.of(new MistmeadowCouncil(), new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Multiple Kithkin still reduce the cost by only one")
    void multipleKithkinReduceCostOnlyOnce() {
        harness.addToBattlefield(player1, new MistmeadowCouncil());
        harness.addToBattlefield(player1, new MistmeadowCouncil());
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The reduction does not remove the green mana requirement")
    void reductionDoesNotRemoveGreenRequirement() {
        harness.addToBattlefield(player1, new MistmeadowCouncil());
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The draw trigger resolves even after Council leaves the battlefield")
    void drawTriggerSurvivesSourceLeaving() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }
}
