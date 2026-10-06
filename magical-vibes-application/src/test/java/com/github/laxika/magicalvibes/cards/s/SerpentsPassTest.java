package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentsPass.class})
class SerpentsPassTest extends BaseCardTest {

    @Test
    @DisplayName("Serpent's Pass enters the battlefield tapped")
    void entersBattlefieldTapped() {
        playPass();

        assertThat(findPermanent(player1, "Serpent's Pass").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Serpent's Pass adds blue mana")
    void addsBlueMana() {
        Permanent pass = addReadyPass(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Serpent's Pass adds black mana")
    void addsBlackMana() {
        Permanent pass = addReadyPass(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Serpent's Pass draws a card")
    void sacrificingDrawsCard() {
        addReadyPass(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Serpent's Pass");
        harness.assertInGraveyard(player1, "Serpent's Pass");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Tapped Serpent's Pass cannot activate either ability")
    void tappedPassCannotActivate() {
        playPass();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Serpent's Pass");
        harness.assertNotInGraveyard(player1, "Serpent's Pass");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Drawing requires all four mana before the land is sacrificed")
    void insufficientManaDoesNotSacrifice() {
        Permanent pass = addReadyPass(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pass.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Serpent's Pass");
        harness.assertNotInGraveyard(player1, "Serpent's Pass");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly controlled noncreature land can produce mana without using the stack")
    void newlyControlledLandProducesManaImmediately() {
        harness.addToBattlefield(player1, new SerpentsPass());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Drawing spends four mana and draws the top card after sacrificing the land")
    void drawSpendsManaAndResolvesWithoutSource() {
        addReadyPass(player1);
        SerpentsPass topCard = new SerpentsPass();
        harness.setLibrary(player1, List.of(topCard, new SerpentsPass()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Serpent's Pass");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertNotOnBattlefield(player1, "Serpent's Pass");
    }
    private void playPass() {
        harness.setHand(player1, List.of(new SerpentsPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
    }

    private Permanent addReadyPass(Player player) {
        Permanent pass = harness.addToBattlefieldAndReturn(player, new SerpentsPass());
        pass.setSummoningSick(false);

        return pass;
    }
}
